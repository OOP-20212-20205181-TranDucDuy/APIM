package com.company.apimanager.controller;

import com.company.apimanager.dto.*;
import com.company.apimanager.entity.PublishedProduct;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntitySerializationOption;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/published-product")
public class PublishedProductController {
    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntitySerialization entitySerialization;

    private final Logger log = LoggerFactory.getLogger(PublishedProductController.class);

    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/detail")
    public String siteInfo(
            @RequestParam UUID product_id, String service_id
    ) {
        try {
            PublishedProduct publishedProduct = dataManager.load(PublishedProduct.class).id(product_id).one();

            if (publishedProduct.getKong_oauth_info() != null && !publishedProduct.getKong_oauth_info().isBlank()) {
                String[] arrOauth = publishedProduct.getKong_oauth_info().split(" ");
                log.info("array oauth {}", (Object) arrOauth);
                for (String s : arrOauth) {
                    String[] data = s.split(";");
                    log.info("array data {}", (Object) data);
                    if (Objects.equals(data[1], service_id)) {
                        return "{\"provision_key\": \"" + data[0] + "\", \"success\": \"" + true + "\"}";
                    }
                }
            }
        } catch (IllegalStateException illegalStateException) {
            log.error("Not found result in PublishedProductController");
        } catch (Exception e) {
            log.error("Error when get published_product info");
            log.error(e.getMessage());
        }

        return entitySerialization.toJson(
                "{\"success\": \" " + false + "\"}",
                null,
                EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
        );
    }

    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/all")
    public String getPublishProduct(@RequestParam UUID catalog_id) {
        List<PublishedProduct> publishedProducts = dataManager.load(PublishedProduct.class).
                query("select p from PublishedProduct p where p.product_site_id = :catalog_id")
                .parameter("catalog_id", catalog_id)
                .list();
        return entitySerialization.toJson(
                publishedProducts,
                null,
                EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
        );
    }

    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/product-catalog")
    public ResponseEntity<ProductCatalogResponseDTO> getProductCatalog() {
        List<PublishedProduct> publishedProducts = dataManager.load(PublishedProduct.class).all().list();

        List<ProductCatalogDTO> productCatalogList = publishedProducts.stream()
                .map(product -> {
                    ProductSourceDTO sourceDTO = new ProductSourceDTO();
                    sourceDTO.setId(product.getId().toString());
                    sourceDTO.setName(product.getName());
                    sourceDTO.setDescription(null);
                    sourceDTO.setCreated_at(null);
                    sourceDTO.setUpdated_at(null);
                    sourceDTO.setVersion_count(0);
                    sourceDTO.setDocument_count(0);
                    sourceDTO.setLatest_version(null);

                    ProductCatalogDTO productCatalogDTO = new ProductCatalogDTO();
                    productCatalogDTO.setIndex("product-catalog");
                    productCatalogDTO.setSource(sourceDTO);

                    return productCatalogDTO;
                })
                .collect(Collectors.toList());

        PageMetaDTO metaDTO = new PageMetaDTO();
        PageDTO pageDTO = new PageDTO();

        pageDTO.setTotal(productCatalogList.size());
        pageDTO.setSize(12);
        pageDTO.setNumber(1);
        metaDTO.setPage(pageDTO);

        ProductCatalogResponseDTO responseDTO = new ProductCatalogResponseDTO();
        responseDTO.setData(productCatalogList);
        responseDTO.setMeta(metaDTO);

        return ResponseEntity.ok(responseDTO);
    }
}
