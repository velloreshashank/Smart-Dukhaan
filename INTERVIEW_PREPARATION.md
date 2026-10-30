# KiranaLink / Smart Dukaan — Java Full Stack Technical Interview Preparation Guide

This comprehensive guide is prepared specifically for technical interviews covering the migration of the **KiranaLink / Smart Dukaan** platform from a Node/Express/MongoDB MERN stack to an enterprise **Java Full Stack (JFS)** architecture using **Java 21, Spring Boot 3.3.4, Spring Data JPA / Hibernate, Spring Security, MySQL 8.0, Flyway, and Maven**.

---

## 1. Executive Architecture Overview

```
                      +---------------------------------------+
                      |       React 18 + TypeScript UI        |
                      |          (Vite / TailwindCSS)         |
                      +-------------------+-------------------+
                                          |
                                    REST / JSON
                               (Port 5001 /api/...)
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                        Spring Boot 3.3.4 Enterprise Backend                       |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  |                            Spring Security Filter Chain                     |  |
|  |           JwtAuthenticationFilter -> SecurityFilterChain -> UserPrincipal  |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         |                                         |
|  +--------------------------------------v--------------------------------------+  |
|  |                             REST Controllers                                |  |
|  |    AuthController, ProductController, BillController, LedgerController,     |  |
|  |    GstController, ExpiryController, WhatsAppController, GroupBuyController  |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         |                                         |
|  +--------------------------------------v--------------------------------------+  |
|  |                            Business Service Layer                           |  |
|  |    - KhataScoreService: 4-factor dynamic mathematical credit scoring engine  |  |
|  |    - InventoryBatchService: First-Expired First-Out (FEFO) allocation       |  |
|  |    - BillService: Atomic checkout, financial totals, credit limit check    |  |
|  |    - LedgerService: Double-entry debt tracking & repayment reconciliation   |  |
|  |    - GstService: Dual CGST/SGST tax computation & ITC ledger                |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         |                                         |
|  +--------------------------------------v--------------------------------------+  |
|  |                     Spring Data JPA & Hibernate 6.x                         |  |
|  |         24 Repositories, Declarative Transactions (@Transactional)           |  |
|  +--------------------------------------+--------------------------------------+  |
+-----------------------------------------|-----------------------------------------+
                                          | JDBC (HikariCP)
                                          v
+-----------------------------------------------------------------------------------+
|                            MySQL 8.0 Relational Database                          |
|             28 Normalized Tables (users, customers, customer_accounts,            |
|             products, inventory_batches, bills, bill_items, ledger_entries,       |
|             gst_invoices, gst_ledger, group_buys, supplier_bills...)              |
|                     Managed via Flyway Migrations (V1, V2)                         |
+-----------------------------------------------------------------------------------+
```

---

## 2. Core Modules: Deep Architectural Breakdown

### Module 1: KhataScore™ AI Credit Scoring Engine
#### Problem Solved:
Small kirana store owners historically rely on gut instinct when advancing credit (*udhaar*), leading to bad debts, defaults, and cash-flow collapse. The KhataScore engine calculates a reproducible, objective creditworthiness score (300 to 900) for every customer using four financial and behavioral dimensions.

#### Implementation Files:
- Service: `com.kiranalink.service.KhataScoreService`
- Entity: `com.kiranalink.entity.Customer`, `com.kiranalink.entity.LedgerEntry`, `com.kiranalink.entity.CustomerAccount`
- Repositories: `CustomerRepository`, `LedgerEntryRepository`, `CustomerAccountRepository`

#### Mathematical Formula:
$$\text{KhataScore} = 300 + 600 \times (0.35 \times \text{PTS} + 0.25 \times \text{CS} + 0.20 \times \text{ORS} + 0.20 \times \text{RS})$$

1. **Promptness Score (PTS - 35% weight)**:
   $$\text{PTS} = \frac{\text{Total Paid Settlements}}{\text{Total Debit Transactions}}$$
   Measures repayment discipline. Customers who consistently clear balances score 1.0.

2. **Credit Utilization Score (CS - 25% weight)**:
   $$\text{CS} = 1.0 - \min\left(1.0, \frac{\text{Current Outstanding Balance}}{\text{Khata Credit Limit}}\right)$$
   High utilization indicates credit distress; low utilization boosts score.

3. **Overdue Ratio Score (ORS - 20% weight)**:
   Penalizes unsettled balances that remain open beyond 30 and 60 days.

4. **Relationship Tenure & Velocity Score (RS - 20% weight)**:
   Rewards customers based on days since account creation and frequency of recurring purchases.

#### Key Code & Annotations:
- `@Service`: Marks as a Spring-managed service bean.
- `@Transactional`: Ensures read-consistent calculation and atomic score/limit persistence.
- Dynamic Khata Limit Escalation:
  - If Score $\ge 750$: Increases credit limit by +25% up to ₹15,000.
  - If Score $< 500$: Freezes credit limit or lowers to ₹1,000 to prevent bad debt.

---

### Module 2: FEFO (First-Expired, First-Out) Inventory Engine
#### Problem Solved:
Kirana stores lose 3–8% of margin due to perishable items (dairy, bread, packaged snacks) expiring unnoticed on shelves. Standard FIFO (First-In, First-Out) assumes batches arrive chronologically, which fails when newly purchased stock has a shorter expiry date than older stock.

#### Implementation Files:
- Service: `com.kiranalink.service.InventoryBatchService`
- Entity: `com.kiranalink.entity.InventoryBatch`, `com.kiranalink.entity.Product`
- Repository: `com.kiranalink.repository.InventoryBatchRepository`

#### Algorithm & Request Flow:
1. When a bill is generated for $Q$ units of Product $P$:
2. Query `InventoryBatchRepository.findAvailableBatchesForFefo(shopkeeperId, productId)`:
   ```sql
   SELECT b FROM InventoryBatch b 
   WHERE b.shopkeeper.id = :shopkeeperId 
     AND b.product.id = :productId 
     AND b.status IN ('active', 'expired') 
     AND b.quantityAvailable > 0 
   ORDER BY b.expiryDate ASC, b.createdAt ASC
   ```
3. Iterate batches in order of ascending expiry date:
   - Batch 1 has 5 units ($E_1 = 10\text{ days}$). Request is 8 units.
   - Deduct 5 units from Batch 1 $\rightarrow$ remaining in Batch 1 = 0, status $\rightarrow$ `'depleted'`.
   - Remaining demand = 3 units.
   - Deduct 3 units from Batch 2 ($E_2 = 40\text{ days}$, 10 units available) $\rightarrow$ remaining in Batch 2 = 7.
4. Atomically decrement `product.stock` and commit batch changes within a single `@Transactional` boundary.

---

### Module 3: Atomic Billing & Double-Entry Khata Accounting
#### Problem Solved:
Preventing financial inconsistencies where a bill is created, but inventory isn't reduced, or customer credit is recorded without validating whether the customer exceeded their khata limit.

#### Implementation Files:
- Controller: `com.kiranalink.controller.BillController`
- Service: `com.kiranalink.service.BillService`
- Repository: `BillRepository`, `BillItemRepository`, `CustomerAccountRepository`, `LedgerEntryRepository`
- Entities: `Bill`, `BillItem`, `LedgerEntry`, `CustomerAccount`

#### Financial Precision:
- Uses `BigDecimal` for every monetary calculation (`totalAmount`, `price`, `balance`, `gstRate`).
- **Never use `double` or `float`** for financial amounts due to binary floating-point representation inaccuracies (e.g. `0.1 + 0.2 = 0.30000000000000004`).

#### Atomicity with `@Transactional`:
If the database connection drops or a stock constraint fails halfway through generating a 10-item bill:
- Spring's `PlatformTransactionManager` issues a `ROLLBACK`.
- No partial bills, phantom ledger entries, or mismatched batch counts are committed to MySQL.

---

### Module 4: GST Compliance & ITC (Input Tax Credit) Ledger
#### Problem Solved:
Kirana stores struggle with complex GST filings (GSTR-1 for sales, GSTR-3B for monthly summary, and GSTR-2B for Input Tax Credit matching).

#### Implementation Files:
- Controller: `com.kiranalink.controller.GstController`
- Service: `com.kiranalink.service.GstService`
- Entities: `GstInvoice`, `GstInvoiceItem`, `GstLedger`, `GstProduct`
- Calculations: `GstInvoiceCalculation`

#### Reverse Tax Extraction Math:
For retail goods with MRP inclusive of GST at rate $R\%$:
$$\text{Base Amount} = \frac{\text{MRP}}{1 + \frac{R}{100}}$$
$$\text{Total GST} = \text{MRP} - \text{Base Amount}$$
$$\text{CGST} = \frac{\text{Total GST}}{2}, \quad \text{SGST} = \frac{\text{Total GST}}{2}$$

#### Net Tax Payable:
$$\text{Net Tax Payable} = \max\left(0, \text{Total Output GST (Sales)} - \text{Total Input GST (Purchases/ITC)}\right)$$

---

### Module 5: Stateless Spring Security & JWT Architecture
#### Problem Solved:
Securing all merchant APIs, enforcing multi-tenant data isolation (Shopkeeper A cannot see or mutate Shopkeeper B's products, bills, or customer ledgers).

#### Implementation Files:
- Filter: `com.kiranalink.security.JwtAuthenticationFilter`
- Token Utility: `com.kiranalink.security.JwtTokenProvider`
- User Details: `com.kiranalink.security.CustomUserDetailsService`, `com.kiranalink.security.UserPrincipal`
- Config: `com.kiranalink.config.SecurityConfig`

#### Filter Flow:
1. Client sends header: `Authorization: Bearer <jwt_token>`.
2. `JwtAuthenticationFilter` intercepts request before reaching `DispatcherServlet`.
3. Validates HMAC-SHA256 signature and checks expiry (`validateToken`).
4. Extracts `userId` from payload (`getUserIdFromToken`).
5. Loads `UserPrincipal` and creates `UsernamePasswordAuthenticationToken`.
6. Sets token in `SecurityContextHolder.getContext().setAuthentication(auth)`.
7. Controllers inject logged-in shopkeeper ID via `@AuthenticationPrincipal UserPrincipal principal`.

---

## 3. High-Probability Interview Questions & Answers

### Q1: Why did you migrate from MongoDB to MySQL for KiranaLink?
> **Answer:**
> "KiranaLink handles financial transactions, customer credit (*khata*), and inventory accounting. In MongoDB, data is non-relational and multi-document consistency requires complex atomic sessions that lack strict schema-level foreign key enforcement.
>
> In retail banking and billing, integrity is paramount:
> 1. **ACID Transactions**: Relational engines guarantee strict Atomicity, Consistency, Isolation, and Durability. A customer credit sale must atomically deduct batch inventory, create a bill, create line items, record a ledger entry, and update the customer account balance.
> 2. **Foreign Key Constraints**: We ensure referential integrity; a bill cannot exist with an invalid customer ID, and orphan records are prevented at the database engine level.
> 3. **Financial Math**: MySQL `DECIMAL(12,2)` paired with Java `BigDecimal` completely eliminates floating-point rounding errors."

---

### Q2: How did you maintain backwards compatibility with the existing React frontend during migration?
> **Answer:**
> "The existing React frontend was designed for MongoDB, expecting strings for IDs, specifically the `_id` field (e.g. `customer._id` or `product._id`).
>
> Rather than rewriting dozens of React components and breaking UI contracts, I used Jackson's `@JsonProperty("_id")` annotation in our JPA entities:
> ```java
> @JsonProperty("_id")
> public String getMongoId() {
>     return id != null ? id.toString() : null;
> }
> ```
> This allowed MySQL auto-increment `BIGINT` primary keys to be used internally for optimal database indexing and foreign key performance, while serializing both `id` and `_id` in JSON responses. Furthermore, the Spring Boot server was configured on port `5001`, exactly matching the frontend's configured `VITE_API_BASE_URL`."

---

### Q3: How do you prevent stock overselling under concurrent checkout requests?
> **Answer:**
> "In our `BillService`, checkout is wrapped in `@Transactional`. For high-concurrency environments, we have two layers of protection:
> 1. **Pessimistic Locking**: In JPA repositories, queries can use `@Lock(LockModeType.PESSIMISTIC_WRITE)` to lock the inventory row (`SELECT ... FOR UPDATE`), ensuring only one thread can deduct stock at a time.
> 2. **Optimistic Locking**: By adding a `@Version private Long version;` field on the `Product` entity, Hibernate automatically verifies the version before writing. If two concurrent transactions attempt to decrement stock simultaneously, the second transaction catches an `OptimisticLockException` and can retry or fail cleanly without corrupting stock counts."

---

### Q4: Explain the difference between `@Transactional` at the method level vs class level. What happens if a `RuntimeException` occurs?
> **Answer:**
> "When `@Transactional` is placed at the class level, all public methods in that class inherit transaction management defaults. When placed on a method, it overrides class-level settings.
>
> By default in Spring:
> - Any unchecked exception (`RuntimeException` or `Error`) triggers an automatic **rollback**.
> - Checked exceptions (`Exception` subclasses) do **not** trigger a rollback unless explicitly configured with `@Transactional(rollbackFor = Exception.class)`.
>
> In our service layer, custom exceptions like `BadRequestException` and `ResourceNotFoundException` extend `RuntimeException`, guaranteeing automatic database rollbacks on business rule failures."

---

### Q5: How does Flyway manage schema versions in a team or CI/CD environment?
> **Answer:**
> "Flyway creates a schema history table called `flyway_schema_history` in MySQL. When the Spring Boot application boots, Flyway inspects `classpath:db/migration` for versioned SQL files (e.g. `V1__init_schema.sql`, `V2__seed_starter_data.sql`).
>
> For every migration:
> 1. It calculates an SHA-256 checksum of the file content.
> 2. If the version has already executed, it verifies the checksum to detect tampering.
> 3. If a new version is detected, it executes the migration inside a transaction and records the execution timestamp, version, and execution duration.
>
> This guarantees reproducible environments across local development, Docker, and production."

---

### Q6: Why did you choose constructor injection over field injection (`@Autowired`)?
> **Answer:**
> "Constructor injection (implemented cleanly using Lombok's `@RequiredArgsConstructor` on `final` fields) is the recommended best practice in Spring:
> 1. **Immutability**: Dependencies are marked `final` and cannot be modified after bean instantiation.
> 2. **Unit Testability**: Dependencies can be easily mocked using Mockito or passed directly in plain JUnit tests without requiring Spring's ReflectionUtils or a slow Spring application context.
> 3. **Fail-Fast Detection**: Circular dependencies or missing required beans are detected at compile-time/startup rather than throwing `NullPointerException` at runtime."

---

### Q7: How does JWT authentication scale compared to traditional HTTP session cookies?
> **Answer:**
> "Traditional session cookies require **stateful** server storage (in memory or a Redis cluster) to validate session IDs on each incoming request. In a distributed multi-instance deployment, sticky sessions or distributed session caches are required.
>
> JWT authentication is **stateless**:
> - The token contains digitally signed claims (user ID, expiration, roles) using HMAC-SHA256 (`Keys.hmacShaKeyFor(secret)`).
> - Any backend instance can verify the signature using the shared secret without querying a database or session store.
> - This enables horizontal scaling behind a round-robin load balancer."

---

## 4. Key Design Decisions & Trade-Offs

| Decision | Alternative Considered | Why We Chose Our Approach |
| :--- | :--- | :--- |
| **Java 21 + Spring Boot 3.3.4** | Java 8 / 11, Spring Boot 2.x | Spring Boot 3 requires Java 17+, brings Jakarta EE 10 namespace, Virtual Threads support, and enhanced Hibernate 6 query generation. |
| **MySQL 8.0 with Flyway** | Hibernate `ddl-auto: update` | `ddl-auto: update` is hazardous in production because it cannot drop constraints or track migrations. Flyway provides deterministic versioning. |
| **FEFO Inventory Model** | Standard FIFO (First-In First-Out) | Essential for groceries; items bought later might expire earlier. Reduces spoilage loss. |
| **`BigDecimal` for Currency** | `double` or `float` | Eliminates IEEE 754 floating-point inaccuracies in Indian Rupee billing, discounts, and GST taxes. |
| **Stateless JWT Security** | Stateful HTTP Sessions | Eliminates server-side session memory, enables load balancing and Docker scaling. |
| **Lombok (`@Data`, `@Builder`)** | Manual boilerplate getters/setters | Reduces hundreds of lines of repetitive boilerplate code while keeping code clean for beginners. |
