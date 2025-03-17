package com.company.apimanager.screen.draftproduct;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.DraftProduct;

@UiController("DraftProduct.browse")
@UiDescriptor("draft-product-browse.xml")
@LookupComponent("draftProductsTable")
public class DraftProductBrowse extends StandardLookup<DraftProduct> {
}