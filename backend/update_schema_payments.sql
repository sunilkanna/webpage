-- Update payments table for Razorpay integration
USE genecare_db;

ALTER TABLE payments 
ADD COLUMN razorpay_order_id VARCHAR(100) AFTER appointment_id,
ADD COLUMN razorpay_payment_id VARCHAR(100) AFTER transaction_id,
ADD COLUMN razorpay_signature VARCHAR(255) AFTER razorpay_payment_id;

-- Ensure transaction_id is still used for general purposes
ALTER TABLE payments MODIFY COLUMN transaction_id VARCHAR(100) DEFAULT NULL;
