package com.company.apimanager.dto;

public class ProductCatalogDTO {
    private String index;
    private ProductSourceDTO source;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public ProductSourceDTO getSource() {
        return source;
    }

    public void setSource(ProductSourceDTO source) {
        this.source = source;
    }
}
