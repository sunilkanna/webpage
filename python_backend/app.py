from flask import Flask, request, jsonify
from flask_cors import CORS
from database import get_db_connection, close_connection
import mysql.connector

app = Flask(__name__)
CORS(app)

@app.route('/health', methods=['GET'])
def health_check():
    conn = get_db_connection()
    if conn:
        close_connection(conn)
        return jsonify({"status": "success", "message": "Python backend is running and connected to DB"})
    else:
        return jsonify({"status": "error", "message": "DB connection failed"}), 500

@app.route('/api/patient/details', methods=['GET'])
def get_patient_details():
    patient_id = request.args.get('patient_id')
    
    if not patient_id:
        return jsonify({"status": "error", "message": "Patient ID required"}), 400
    
    conn = get_db_connection()
    if not conn:
        return jsonify({"status": "error", "message": "Database connection failed"}), 500
    
    cursor = conn.cursor(dictionary=True)
    
    try:
        # 1. Fetch Basic Profile Info
        profile_sql = """
            SELECT u.id, u.full_name as name, u.email,
            pp.date_of_birth, pp.gender, pp.phone, pp.address,
            pp.height, pp.weight, pp.blood_type,
            TIMESTAMPDIFF(YEAR, pp.date_of_birth, CURDATE()) AS age
            FROM users u
            LEFT JOIN patient_profiles pp ON u.id = pp.user_id
            WHERE u.id = %s AND LOWER(u.user_type) = 'patient'
        """
        cursor.execute(profile_sql, (patient_id,))
        profile = cursor.fetchone()
        
        if not profile:
            return jsonify({"status": "error", "message": "Patient not found"}), 404
        
        # 2. Fetch Medical History
        history_sql = """
            SELECT id, condition_name, diagnosis_date, medications, allergies, surgeries 
            FROM medical_history 
            WHERE user_id = %s 
            ORDER BY diagnosis_date DESC
        """
        cursor.execute(history_sql, (patient_id,))
        history_rows = cursor.fetchall()
        medical_history = []
        for row in history_rows:
            medical_history.append({
                "id": row['id'],
                "condition_name": row['condition_name'] or 'Unknown',
                "diagnosis_date": str(row['diagnosis_date']) if row['diagnosis_date'] else '',
                "medications": row['medications'] or '',
                "allergies": row['allergies'] or '',
                "surgeries": row['surgeries'] or ''
            })
            
        # 3. Fetch Genetic Risks
        risk_sql = """
            SELECT risk_category, details, assessed_at 
            FROM risk_assessments 
            WHERE patient_id = %s 
            ORDER BY assessed_at DESC LIMIT 5
        """
        cursor.execute(risk_sql, (patient_id,))
        risk_rows = cursor.fetchall()
        genetic_risks = []
        for row in risk_rows:
            genetic_risks.append({
                "category": row['risk_category'] or 'General',
                "assessed_at": str(row['assessed_at']) if row['assessed_at'] else ''
            })
            
        return jsonify({
            "status": "success",
            "patient": {
                "id": profile['id'],
                "name": profile['name'],
                "email": profile['email'],
                "age": profile['age'] or 0,
                "gender": profile['gender'] or 'Unknown',
                "phone": profile['phone'] or '',
                "address": profile['address'] or '',
                "date_of_birth": str(profile['date_of_birth']) if profile['date_of_birth'] else '',
                "height": profile['height'] or 'N/A',
                "weight": profile['weight'] or 'N/A',
                "blood_type": profile['blood_type'] or 'N/A',
                "medical_history": medical_history,
                "genetic_risks": genetic_risks
            }
        })
        
    except mysql.connector.Error as err:
        return jsonify({"status": "error", "message": str(err)}), 500
    finally:
        close_connection(conn, cursor)

@app.route('/api/patient/update', methods=['POST'])
def update_profile():
    data = request.get_json() or request.form
    
    user_id = data.get('user_id')
    full_name = str(data.get('full_name', '')).strip()
    dob = str(data.get('date_of_birth', '')).strip()
    gender = str(data.get('gender', '')).strip()
    phone = "".join(filter(str.isdigit, str(data.get('phone', ''))))
    address = str(data.get('address', '')).strip()
    height = str(data.get('height', '')).strip()
    weight = str(data.get('weight', '')).strip()
    blood_type = str(data.get('blood_type', '')).strip()
    
    if not user_id:
        return jsonify({"status": "error", "message": "User ID is required"}), 400
        
    conn = get_db_connection()
    if not conn:
        return jsonify({"status": "error", "message": "Database connection failed"}), 500
        
    cursor = conn.cursor()
    
    try:
        conn.start_transaction()
        
        # 1. Update Full Name in 'users' table if provided
        if full_name:
            cursor.execute("UPDATE users SET full_name = %s WHERE id = %s", (full_name, user_id))
            
        # 2. Insert/Update fields in 'patient_profiles' table
        stmt2 = """
            INSERT INTO patient_profiles (user_id, date_of_birth, gender, phone, address, height, weight, blood_type) 
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE date_of_birth=%s, gender=%s, phone=%s, address=%s, height=%s, weight=%s, blood_type=%s
        """
        vals = (user_id, dob, gender, phone, address, height, weight, blood_type,
                dob, gender, phone, address, height, weight, blood_type)
        cursor.execute(stmt2, vals)
        
        conn.commit()
        return jsonify({"status": "success", "message": "Profile updated successfully"})
        
    except mysql.connector.Error as err:
        conn.rollback()
        return jsonify({"status": "error", "message": f"Error updating profile: {str(err)}"}), 500
    finally:
        close_connection(conn, cursor)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
