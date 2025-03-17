package com.company.apimanager.dto;

import java.util.List;

public class ProductCatalogResponseDTO {
    private List<ProductCatalogDTO> data;
    private PageMetaDTO meta;

    public List<ProductCatalogDTO> getData() {
        return data;
    }

    public void setData(List<ProductCatalogDTO> data) {
        this.data = data;
    }

    public PageMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(PageMetaDTO meta) {
        this.meta = meta;
    }
}
