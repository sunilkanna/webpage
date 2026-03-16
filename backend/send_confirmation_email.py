import smtplib
import sys
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

def send_email(patient_email, patient_name, counselor_name, appt_date, appt_time, meeting_link):
    # SMTP Configuration (Hardcoded for this standalone service, or could be read from config)
    SMTP_HOST = "smtp.gmail.com"
    SMTP_PORT = 587
    SMTP_USERNAME = "kannasuneel85@gmail.com"
    SMTP_PASSWORD = "wdjm tffk qelx llwg"
    SMTP_FROM_NAME = "GeneCare"

    try:
        # Create message container
        msg = MIMEMultipart('alternative')
        msg['Subject'] = f"GeneCare - Your Session is Confirmed! ✅"
        msg['From'] = f"{SMTP_FROM_NAME} <{SMTP_USERNAME}>"
        msg['To'] = patient_email

        # HTML Content
        html = f"""
        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 15px; background-color: #fcfcfc;">
            <div style="background: linear-gradient(135deg, #00acc1, #007c91); padding: 40px; border-radius: 12px; text-align: center; color: white;">
                <h1 style="margin: 0; font-size: 28px; letter-spacing: 1px;">Session Confirmed!</h1>
                <p style="opacity: 0.9; margin-top: 10px;">Your health journey continues</p>
            </div>
            
            <div style="padding: 30px; color: #333;">
                <p style="font-size: 18px;">Hi <strong>{patient_name}</strong>,</p>
                <p>Great news! Your genetic counseling session has been confirmed with our specialist.</p>
                
                <div style="background-color: #fff; border: 1px solid #e0f2f1; border-left: 5px solid #00acc1; padding: 20px; border-radius: 8px; margin: 25px 0;">
                    <p style="margin: 5px 0; color: #555;"><strong>👨‍⚕️ Counselor:</strong> Dr. {counselor_name}</p>
                    <p style="margin: 5px 0; color: #555;"><strong>📅 Date:</strong> {appt_date}</p>
                    <p style="margin: 5px 0; color: #555;"><strong>⏰ Time:</strong> {appt_time}</p>
                </div>
                
                <p style="text-align: center; font-size: 16px; color: #555;">To join your session at the scheduled time:</p>
                
                <div style="background-color: #f0f7f8; padding: 20px; border-radius: 10px; margin: 20px 0;">
                    <p style="margin: 0 0 10px 0;"><strong>📱 On Mobile:</strong> Open the <strong>GeneCare App</strong></p>
                    <p style="margin: 0;"><strong>💻 On Computer:</strong> Click the button below:</p>
                    
                    <div style="text-align: center; margin-top: 15px;">
                        <a href="{meeting_link}" style="background-color: #00acc1; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 15px; display: inline-block;">Join via Website</a>
                    </div>
                </div>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                <p style="font-size: 11px; color: #bbb; text-align: center;">
                    This is an automated message from GeneCare. Please do not reply to this email.
                </p>
            </div>
        </div>
        """

        # Plain text fallback
        text = f"Hi {patient_name}, your session with Dr. {counselor_name} on {appt_date} at {appt_time} has been confirmed. Please join using the GeneCare app."

        msg.attach(MIMEText(text, 'plain'))
        msg.attach(MIMEText(html, 'html'))

        # Connect and send
        server = smtplib.SMTP(SMTP_HOST, SMTP_PORT)
        server.starttls()
        server.login(SMTP_USERNAME, SMTP_PASSWORD)
        server.sendmail(SMTP_USERNAME, patient_email, msg.as_string())
        server.quit()

        print(f"SUCCESS: Email sent to {patient_email}")
        return True

    except Exception as e:
        print(f"ERROR: {str(e)}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 7:
        print("Usage: python send_confirmation_email.py <email> <patient_name> <counselor_name> <date> <time> <link>")
        sys.exit(1)

    patient_email = sys.argv[1]
    patient_name = sys.argv[2]
    counselor_name = sys.argv[3]
    appt_date = sys.argv[4]
    appt_time = sys.argv[5]
    meeting_link = sys.argv[6]

    send_email(patient_email, patient_name, counselor_name, appt_date, appt_time, meeting_link)
