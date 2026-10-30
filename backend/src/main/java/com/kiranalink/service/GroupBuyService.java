package com.kiranalink.service;

import com.kiranalink.dto.groupbuy.GroupBuyCreateRequest;
import com.kiranalink.dto.groupbuy.GroupBuyJoinRequest;
import com.kiranalink.entity.GroupBuy;
import com.kiranalink.entity.User;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.GroupBuyRepository;
import com.kiranalink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;

    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<GroupBuy> getDeals(Double latitude, Double longitude, Double radiusKm) {
        List<GroupBuy> allDeals = groupBuyRepository.findAll();

        if (latitude != null && longitude != null) {
            double maxDist = (radiusKm != null && radiusKm > 0) ? radiusKm : 5.0;
            return allDeals.stream()
                    .filter(d -> {
                        if (d.getLatitude() == null || d.getLongitude() == null) return true;
                        double dist = calculateHaversineDistanceKm(latitude, longitude, d.getLatitude().doubleValue(), d.getLongitude().doubleValue());
                        return dist <= maxDist;
                    })
                    .toList();
        }

        return allDeals;
    }

    @Transactional
    public GroupBuy createDeal(GroupBuyCreateRequest request) {
        User shopkeeper = null;
        if (request.getShopkeeperId() != null) {
            shopkeeper = userRepository.findById(request.getShopkeeperId()).orElse(null);
        }
        if (shopkeeper == null) {
            shopkeeper = userRepository.findAll().stream().findFirst().orElse(null);
        }

        String hostShop = request.getHostShopName();
        if (hostShop == null && shopkeeper != null) {
            hostShop = shopkeeper.getShopName() != null ? shopkeeper.getShopName() : shopkeeper.getName();
        }

        GroupBuy deal = new GroupBuy();
        deal.setShopkeeper(shopkeeper);
        deal.setGroupName(request.getTitle());
        deal.setTotalAmount(request.getGroupPrice().multiply(BigDecimal.valueOf(request.getTargetUnits())));
        deal.setMarketPrice(request.getOriginalPrice());
        deal.setDealPrice(request.getGroupPrice());
        deal.setTargetUnits(request.getTargetUnits());
        deal.setCurrentUnits(0);
        deal.setAnchorShop(hostShop != null ? hostShop : "Local Hub");
        deal.setExpiresAt(request.getDeadline());
        deal.setStatus("active");
        if (request.getLatitude() != null) deal.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if (request.getLongitude() != null) deal.setLongitude(BigDecimal.valueOf(request.getLongitude()));

        return groupBuyRepository.save(deal);
    }

    @Transactional
    public GroupBuy joinDeal(Long dealId, GroupBuyJoinRequest request) {
        GroupBuy deal = groupBuyRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found with id: " + dealId));

        int units = request.getUnits() != null ? request.getUnits() : 1;
        deal.setCurrentUnits((deal.getCurrentUnits() != null ? deal.getCurrentUnits() : 0) + units);

        if (request.getCustomerId() != null) {
            String custIdStr = request.getCustomerId().toString();
            if (!deal.getMembers().contains(custIdStr)) {
                deal.getMembers().add(custIdStr);
            }
        }

        if (deal.getCurrentUnits() >= deal.getTargetUnits()) {
            deal.setStatus("completed");
        }

        return groupBuyRepository.save(deal);
    }
}
