package com.company.apimanager.screen.visibilytype;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.VisibilyType;

@UiController("VisibilyType.browse")
@UiDescriptor("visibily-type-browse.xml")
@LookupComponent("visibilyTypesTable")
public class VisibilyTypeBrowse extends StandardLookup<VisibilyType> {
}