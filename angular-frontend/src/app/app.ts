import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private http = inject(HttpClient);
  
  products: Product[] = [];
  cart: { product: Product, quantity: number }[] = [];
  
  // Notification state
  showNotification = false;
  notificationMessage = '';
  
  private apiUrl = 'http://localhost:8080/api';

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.http.get<Product[]>(`${this.apiUrl}/products`).subscribe({
      next: (data) => this.products = data,
      error: (err) => console.error('Failed to load products', err)
    });
  }

  addToCart(product: Product) {
    if (product.stockQuantity <= 0) return;
    
    const existing = this.cart.find(item => item.product.id === product.id);
    if (existing) {
      if (existing.quantity < product.stockQuantity) {
        existing.quantity++;
      }
    } else {
      this.cart.push({ product, quantity: 1 });
    }
  }

  get cartTotal() {
    return this.cart.reduce((total, item) => total + (item.product.price * item.quantity), 0);
  }

  placeOrder() {
    if (this.cart.length === 0) return;
    
    // For simplicity, place order for the first item in the cart
    const item = this.cart[0];
    const orderRequest = {
      productId: item.product.id,
      quantity: item.quantity
    };

    this.http.post(`${this.apiUrl}/orders`, orderRequest, { responseType: 'text' }).subscribe({
      next: (response) => {
        this.triggerNotification(response);
        this.cart = [];
        this.loadProducts(); // Refresh stock
      },
      error: (err) => {
        this.triggerNotification('Error placing order: ' + err.message);
        console.error(err);
      }
    });
  }

  triggerNotification(message: string) {
    this.notificationMessage = message;
    this.showNotification = true;
    setTimeout(() => {
      this.showNotification = false;
    }, 4000);
  }
}
