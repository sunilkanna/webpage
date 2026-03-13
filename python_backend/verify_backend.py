import requests
import sys

BASE_URL = "http://localhost:5000"

def test_health():
    print("Testing Health Check...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"Status: {response.status_code}")
        print(f"Body: {response.json()}")
    except Exception as e:
        print(f"Error: {e}")

def test_get_details(patient_id):
    print(f"\nTesting Get Patient Details (ID: {patient_id})...")
    try:
        response = requests.get(f"{BASE_URL}/api/patient/details", params={"patient_id": patient_id})
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            print("Successfully fetched patient details.")
            # print(response.json())
        else:
            print(f"Failed: {response.json()}")
    except Exception as e:
        print(f"Error: {e}")

def test_update_profile(user_id):
    print(f"\nTesting Update Profile (ID: {user_id})...")
    payload = {
        "user_id": user_id,
        "full_name": "Antigravity Test",
        "height": "180",
        "weight": "75",
        "blood_type": "O+"
    }
    try:
        response = requests.post(f"{BASE_URL}/api/patient/update", json=payload)
        print(f"Status: {response.status_code}")
        print(f"Body: {response.json()}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_health()
    test_get_details(1)  # Using valid ID 1
    test_update_profile(1)
    test_get_details(1) # Verify update
