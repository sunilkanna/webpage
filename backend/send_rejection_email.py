import smtplib
import sys
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

def send_email(patient_email, patient_name, counselor_name, appt_date, appt_time, rejection_reason):
    SMTP_HOST = "smtp.gmail.com"
    SMTP_PORT = 587
    SMTP_USERNAME = "kannasuneel85@gmail.com"
    SMTP_PASSWORD = "wdjm tffk qelx llwg"
    SMTP_FROM_NAME = "GeneCare"

    try:
        msg = MIMEMultipart('alternative')
        msg['Subject'] = f"GeneCare - Session Request Declined"
        msg['From'] = f"{SMTP_FROM_NAME} <{SMTP_USERNAME}>"
        msg['To'] = patient_email

        html = f"""
        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 15px; background-color: #fcfcfc;">
            <div style="background: linear-gradient(135deg, #d32f2f, #b71c1c); padding: 40px; border-radius: 12px; text-align: center; color: white;">
                <h1 style="margin: 0; font-size: 28px; letter-spacing: 1px;">Session Request Declined</h1>
                <p style="opacity: 0.9; margin-top: 10px;">We're sorry, your request could not be accommodated</p>
            </div>
            
            <div style="padding: 30px; color: #333;">
                <p style="font-size: 18px;">Hi <strong>{patient_name}</strong>,</p>
                <p>Unfortunately, your genetic counseling session request has been declined by the counselor. Please see the details below.</p>
                
                <div style="background-color: #fff; border: 1px solid #ffcdd2; border-left: 5px solid #d32f2f; padding: 20px; border-radius: 8px; margin: 25px 0;">
                    <p style="margin: 5px 0; color: #555;"><strong>👨‍⚕️ Counselor:</strong> Dr. {counselor_name}</p>
                    <p style="margin: 5px 0; color: #555;"><strong>📅 Date:</strong> {appt_date}</p>
                    <p style="margin: 5px 0; color: #555;"><strong>⏰ Time:</strong> {appt_time}</p>
                </div>
                
                <div style="background-color: #fff3e0; border: 1px solid #ffe0b2; padding: 20px; border-radius: 8px; margin: 25px 0;">
                    <p style="margin: 0 0 8px 0; font-weight: bold; color: #e65100;">📝 Reason for Decline:</p>
                    <p style="margin: 0; color: #555; font-style: italic;">{rejection_reason}</p>
                </div>
                
                <div style="background-color: #e8f5e9; padding: 20px; border-radius: 10px; margin: 20px 0; text-align: center;">
                    <p style="margin: 0; font-size: 15px; color: #2e7d32;">
                        💡 <strong>Don't worry!</strong> You can book a session with another available counselor or try a different time slot.
                    </p>
                </div>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                <p style="font-size: 11px; color: #bbb; text-align: center;">
                    This is an automated message from GeneCare. Please do not reply to this email.
                </p>
            </div>
        </div>
        """

        text = f"Hi {patient_name}, your session request with Dr. {counselor_name} on {appt_date} at {appt_time} has been declined. Reason: {rejection_reason}. You can book a session with another counselor or try a different time slot."

        msg.attach(MIMEText(text, 'plain'))
        msg.attach(MIMEText(html, 'html'))

        server = smtplib.SMTP(SMTP_HOST, SMTP_PORT)
        server.starttls()
        server.login(SMTP_USERNAME, SMTP_PASSWORD)
        server.sendmail(SMTP_USERNAME, patient_email, msg.as_string())
        server.quit()

        print(f"SUCCESS: Rejection email sent to {patient_email}")
        return True

    except Exception as e:
        print(f"ERROR: {str(e)}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 7:
        print("Usage: python send_rejection_email.py <email> <patient_name> <counselor_name> <date> <time> <reason>")
        sys.exit(1)

    send_email(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6])
