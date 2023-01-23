package de.hdawg.rankinginfo.service.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ListingInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {

    }

}
