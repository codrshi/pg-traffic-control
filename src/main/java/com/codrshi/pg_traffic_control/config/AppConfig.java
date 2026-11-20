package com.codrshi.pg_traffic_control.config;

import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import com.codrshi.pg_traffic_control.util.preflight_inspection_stages.CaptureRunawayCountPfiStage;
import com.codrshi.pg_traffic_control.util.preflight_inspection_stages.ConnCountPfiStage;
import com.codrshi.pg_traffic_control.util.preflight_inspection_stages.DbTypeCheckPfiStage;
import com.codrshi.pg_traffic_control.util.preflight_inspection_stages.DestinationConnCheckPfiStage;
import com.codrshi.pg_traffic_control.util.preflight_inspection_stages.OriginConnCheckPfiStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PreflightInspectionStage preflightInspectionChain() {
        return new CaptureRunawayCountPfiStage(
                        new DbTypeCheckPfiStage(
                                new ConnCountPfiStage(
                                        new OriginConnCheckPfiStage(
                                                new DestinationConnCheckPfiStage(null)
                                        )
                                )
                        )
                );
    }

}
