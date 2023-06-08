package com.example.springboot.controllers;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProduceRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {

    @Autowired
    ProduceRepository productRep;

    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productDto){
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRep.save(productModel));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getProdcts(){
        List<ProductModel> productList = productRep.findAll();
        if (!productList.isEmpty()){
            productList.forEach((product) -> {
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getProductById(id)).withSelfRel());
            });
        }
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable(value = "id")UUID id){
        Optional<ProductModel> product = productRep.findById(id);
        if(product.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        product.get().add(linkTo(methodOn(ProductController.class).getProdcts()).withRel("Products List"));
        return ResponseEntity.status(HttpStatus.OK).body(product.get());
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<Object> updateProduct(
        @PathVariable(value = "id") UUID id,
        @RequestBody ProductRecordDto requestProduct
    ){
        Optional<ProductModel> optionalProductModel = productRep.findById(id);

        if (optionalProductModel.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not Found");
        }

        var product = optionalProductModel.get();
        BeanUtils.copyProperties(requestProduct, product);
        return ResponseEntity.status(HttpStatus.OK).body(productRep.save(product));
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> optionalProduct = productRep.findById(id);

        if (optionalProduct.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productRep.delete(optionalProduct.get());

        return ResponseEntity.status(HttpStatus.OK).body("Product Deleted.");
    }
}
