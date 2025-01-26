package com.sagar.reactdigitaldealsbackend.api;

import com.sagar.reactdigitaldealsbackend.model.Cart;
import com.sagar.reactdigitaldealsbackend.model.Cartitem;
import com.sagar.reactdigitaldealsbackend.model.Product;
import com.sagar.reactdigitaldealsbackend.model.User;
import com.sagar.reactdigitaldealsbackend.service.CartService;
import com.sagar.reactdigitaldealsbackend.service.CartitemService;
import com.sagar.reactdigitaldealsbackend.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private CartitemService cartitemService;
    @Autowired
    private ProductService productService;

    @GetMapping("/")
    private List<Cartitem> getCartItems(HttpSession session) {
        User user = (User) session.getAttribute("activeUser");
        Cart cart = cartService.getCartByUser(user);
        System.out.println(cartitemService.getAllCartitemsByCart(cart));
        return cartitemService.getAllCartitemsByCart(cart);
    }

    @PostMapping("/add/{id}")
    private void addCartitem(@PathVariable int id, @RequestBody Map<String, Integer> requestBody, HttpSession session) {
        // Retrieve quantity from the request body
        int quantity = requestBody.get("quantity");

        // Get the active user from the session
        User activeUser = (User) session.getAttribute("activeUser");
        System.out.println(activeUser);

        // Get the product details
        Product product = productService.getProductById(id);

        // Create and save the Cartitem
        Cartitem ci = new Cartitem();
        ci.setProduct(product);
        ci.setPurchaseQuantity(quantity); // Set the received quantity
        ci.setPurchaseAmount(product.getDiscountedPrice() * quantity); // Calculate total purchase amount
        ci.setCart(cartService.getCartByUser(activeUser));
        cartitemService.addCartitem(ci);

        // Update the cart's total amount
        Cart c = cartService.getCartByUser(activeUser);
        c.setTotalAmount(c.getTotalAmount() + (product.getDiscountedPrice() * quantity));
        cartService.updateCart(c);
    }


    @DeleteMapping("/delete/{id}")
    private void deleteCartitem(@PathVariable int id, HttpSession session){

        User activeUser = (User) session.getAttribute("activeUser");
        System.out.println(activeUser);

        Cartitem ci = cartitemService.getCartitemById(id);

        Cart c = cartService.getCartByUser(activeUser);
        c.setTotalAmount(c.getTotalAmount() - ci.getPurchaseAmount());
        cartService.updateCart(c);

        cartitemService.deleteCartitem(id);
    }

    @PutMapping("/update/{id}")
    public void updateCartItemQuantity(@PathVariable int id, @RequestBody Map<String, Integer> requestBody, HttpSession httpSession) {
        User activeUser = (User) httpSession.getAttribute("activeUser");
        int quantity = requestBody.get("quantity");
        Cartitem item = cartitemService.getCartitemById(id);
        item.setPurchaseQuantity(quantity);
        item.setPurchaseAmount(item.getProduct().getDiscountedPrice() * quantity);
        cartitemService.updateCartitem(item);

        // Update the cart's total amount
        Cart cart = cartService.getCartByUser(activeUser);
        double newTotal = cartService.calculateCartTotal(cart); // Calculate total again
        cart.setTotalAmount(newTotal);
        cartService.updateCart(cart);
    }

}
