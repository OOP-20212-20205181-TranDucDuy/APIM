package com.company.apimanager.screen.plan;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.Plan;

@UiController("Plan_.edit")
@UiDescriptor("plan-edit.xml")
@EditedEntityContainer("planDc")
public class PlanEdit extends StandardEditor<Plan> {
}