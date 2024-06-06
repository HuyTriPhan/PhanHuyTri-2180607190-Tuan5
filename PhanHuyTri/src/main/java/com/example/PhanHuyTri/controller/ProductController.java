package com.example.PhanHuyTri.controller;

import com.example.PhanHuyTri.model.Product;
import com.example.PhanHuyTri.service.CategoryService;
import com.example.PhanHuyTri.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/products")
public class ProductController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // Display a list of all products
    @GetMapping
    public String showProductList(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            model.addAttribute("products", productService.searchByName(query));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        return "categories/products/product-list";
    }

    // For adding a new product
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories()); // Load categories
        return "categories/products/add-product";
    }

    // Process the form for adding a new product
    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute Product product, BindingResult result,
                             @RequestParam("image") MultipartFile file, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "categories/products/add-product";
        }

        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR, fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                product.setImageUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                result.rejectValue("image", "error.product", "Could not save image file");
                return "categories/products/add-product";
            }
        }

        productService.addProduct(product);
        return "redirect:/products";
    }

    // For editing a product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories()); // Load categories
        return "categories/products/update-product";
    }

    // Process the form for updating a product
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute Product product,
                                BindingResult result, @RequestParam("image") MultipartFile file, Model model) {
        if (result.hasErrors()) {
            product.setId(id); // Set id to keep it in the form in case of errors
            model.addAttribute("categories", categoryService.getAllCategories());
            return "categories/products/update-product";
        }

        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR, fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                product.setImageUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                result.rejectValue("image", "error.product", "Could not save image file");
                return "categories/products/update-product";
            }
        } else {
            // If no new image file is uploaded, keep the old image URL
            Product existingProduct = productService.getProductById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
            product.setImageUrl(existingProduct.getImageUrl());
        }

        productService.updateProduct(product);
        return "redirect:/products";
    }

    // Handle request to delete a product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
}
