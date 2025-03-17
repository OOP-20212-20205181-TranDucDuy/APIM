package com.company.apimanager.screen.draftplan;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.DraftPlan;

@UiController("DraftPlan.edit")
@UiDescriptor("draft-plan-edit.xml")
@EditedEntityContainer("draftPlanDc")
public class DraftPlanEdit extends StandardEditor<DraftPlan> {
}