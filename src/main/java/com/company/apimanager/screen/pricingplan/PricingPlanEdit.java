package com.company.apimanager.screen.pricingplan;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PricingPlan;

@UiController("PricingPlan.edit")
@UiDescriptor("pricing-plan-edit.xml")
@EditedEntityContainer("pricingPlanDc")
public class PricingPlanEdit extends StandardEditor<PricingPlan> {
}