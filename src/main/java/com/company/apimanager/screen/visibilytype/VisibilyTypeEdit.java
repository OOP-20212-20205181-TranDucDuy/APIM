package com.company.apimanager.screen.visibilytype;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.VisibilyType;

@UiController("VisibilyType.edit")
@UiDescriptor("visibily-type-edit.xml")
@EditedEntityContainer("visibilyTypeDc")
public class VisibilyTypeEdit extends StandardEditor<VisibilyType> {
}