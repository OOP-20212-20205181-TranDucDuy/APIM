package com.company.apimanager.screen.pricingplan;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PricingPlan;

@UiController("PricingPlan.browse")
@UiDescriptor("pricing-plan-browse.xml")
@LookupComponent("pricingPlansTable")
public class PricingPlanBrowse extends StandardLookup<PricingPlan> {
}