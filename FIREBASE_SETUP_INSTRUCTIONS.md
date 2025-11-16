# Firebase Database Setup Instructions

## How to Import the Database Structure

### Step 1: Set up Firebase Realtime Database Rules

1. Go to Firebase Console → Your Project → Realtime Database
2. Click on the **Rules** tab
3. Copy the content from `firebase_database_structure.json`
4. Paste it into the Rules editor
5. Click **Publish**

### Step 2: Import Database Data

1. In Firebase Console → Realtime Database → **Data** tab
2. Click the **⋮** (three dots) menu at the top
3. Select **Import JSON**
4. Select the `firebase_database_data.json` file
5. Click **Import**

**Note:** Only import the data (not the rules) - the rules are already set up in Step 1.

## Database Structure Overview

### AppSettings
- **currency**: "USD"
- **currencySymbol**: "$"
- **shippingFee**: 10 (USD)
- **taxRate**: 0.1 (10%)
- **freeShippingThreshold**: 100 (USD)
- Other app configuration settings

### Banner
- Image carousel banners for homepage
- Each banner has: id, title, url, link, order, active status

### Category
- Product categories (Women, Men, Kids, Accessories, etc.)
- Each category has: id, title, picPath

### Items
- Product catalog
- Each item has: title, description, price, oldPrice, offPercent, size, color, picUrl, review, rating

### Orders
- User orders are stored under `Orders/{userId}/{orderId}`
- Each order has: orderId, userId, orderDate, status, items, subtotal, delivery, tax, total
- Status values: "On Progress", "Completed", "Delivered", "Pending"

### Users
- User profiles stored under `Users/{uid}`
- Each user can have: wishlist, cart, addresses sub-nodes

### Coupons
- Discount coupons with codes, discount amounts, expiry dates

## Important Notes

1. **Currency**: All prices are now in USD with "$" symbol
2. **Shipping Fee**: Changed from 30000 VND to 10 USD
3. **Free Shipping Threshold**: Changed from 500000 VND to 100 USD
4. **Tax Rate**: 10% (0.1) remains the same
5. **Orders**: Make sure all new orders include the `orderDate` field (required by validation rules)

## Sample Order Structure

```json
{
  "Orders": {
    "user_uid_here": {
      "order_123": {
        "orderId": "order_123",
        "userId": "user_uid_here",
        "orderDate": 1704067200000,
        "status": "On Progress",
        "subtotal": 99.99,
        "tax": 9.99,
        "delivery": 10.00,
        "total": 119.98,
        "items": [
          {
            "title": "Summer Dress",
            "price": 29.99,
            "NumberinCart": 1,
            "color": ["Red"],
            "picUrl": ["https://..."],
            ...
          }
        ]
      }
    }
  }
}
```

## Security Rules Summary

- **Users**: Users can only read/write their own data
- **AppSettings**: Read-only for all authenticated users
- **Banner/Category/Items**: Public read, no write
- **Orders**: Users can only read/write their own orders under `Orders/{uid}/{orderId}`
- **Reviews**: Public read, authenticated users can write
- **Coupons**: Authenticated users can read, no write
- **Notifications**: Users can only read their own notifications

## Important: Orders Path Structure

The app stores orders in the following structure:
```
Orders/
  └── {userId}/
      └── {orderId}/
          ├── orderId: "..."
          ├── userId: "..."
          ├── orderDate: timestamp
          ├── status: "On Progress" | "Completed" | "Delivered"
          ├── subtotal: number
          ├── tax: number
          ├── delivery: number
          ├── total: number
          └── items: array
```

The Firebase rules allow users to:
- Create new orders under their own `{userId}` path
- Read and update only their own orders
- Validate that all required fields are present when creating an order

