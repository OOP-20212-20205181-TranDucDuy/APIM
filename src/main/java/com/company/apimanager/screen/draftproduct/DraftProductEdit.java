package com.company.apimanager.screen.draftproduct;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.DraftProduct;

@UiController("DraftProduct.edit")
@UiDescriptor("draft-product-edit.xml")
@EditedEntityContainer("draftProductDc")
public class DraftProductEdit extends StandardEditor<DraftProduct> {
}