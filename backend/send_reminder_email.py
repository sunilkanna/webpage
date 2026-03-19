import smtplib
import sys
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

def send_email(patient_email, patient_name, counselor_name, appt_date, appt_time, meeting_link):
    SMTP_HOST = "smtp.gmail.com"
    SMTP_PORT = 587
    SMTP_USERNAME = "kannasuneel85@gmail.com"
    SMTP_PASSWORD = "wdjm tffk qelx llwg"
    SMTP_FROM_NAME = "GeneCare"

    try:
        msg = MIMEMultipart('alternative')
        msg['Subject'] = f"GeneCare - Your Session Starts in 10 Minutes! ⏰"
        msg['From'] = f"{SMTP_FROM_NAME} <{SMTP_USERNAME}>"
        msg['To'] = patient_email

        html = f"""
        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 15px; background-color: #fcfcfc;">
            <div style="background: linear-gradient(135deg, #ff9800, #f57c00); padding: 40px; border-radius: 12px; text-align: center; color: white;">
                <h1 style="margin: 0; font-size: 28px; letter-spacing: 1px;">⏰ Session Starting Soon!</h1>
                <p style="opacity: 0.9; margin-top: 10px;">Your consultation begins in just a few minutes</p>
            </div>
            
            <div style="padding: 30px; color: #333;">
                <p style="font-size: 18px;">Hi <strong>{patient_name}</strong>,</p>
                <p>This is a friendly reminder that your genetic counseling session is about to begin!</p>
                
                <div style="background-color: #fff; border: 1px solid #ffe0b2; border-left: 5px solid #ff9800; padding: 20px; border-radius: 8px; margin: 25px 0;">
                    <p style="margin: 5px 0; color: #555;"><strong>👨‍⚕️ Counselor:</strong> Dr. {counselor_name}</p>
                    <p style="margin: 5px 0; color: #555;"><strong>📅 Date:</strong> {appt_date}</p>
                    <p style="margin: 5px 0; color: #555;"><strong>⏰ Time:</strong> {appt_time}</p>
                </div>
                
                <div style="background-color: #fff3e0; padding: 20px; border-radius: 10px; margin: 20px 0; text-align: center;">
                    <p style="margin: 0 0 15px 0; font-size: 16px; font-weight: bold; color: #e65100;">🔔 Please get ready for your session!</p>
                    <p style="margin: 0 0 10px 0;"><strong>📱 On Mobile:</strong> Open the <strong>GeneCare App</strong></p>
                    <p style="margin: 0;"><strong>💻 On Computer:</strong> Click the button below:</p>
                    
                    <div style="text-align: center; margin-top: 15px;">
                        <a href="{meeting_link}" style="background-color: #ff9800; color: white; padding: 14px 35px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; display: inline-block;">Join Session Now</a>
                    </div>
                </div>

                <div style="background-color: #e8f5e9; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <p style="margin: 0; font-size: 13px; color: #2e7d32;">
                        💡 <strong>Tips:</strong> Make sure your camera and microphone are working. Find a quiet, well-lit space for your consultation.
                    </p>
                </div>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                <p style="font-size: 11px; color: #bbb; text-align: center;">
                    This is an automated reminder from GeneCare. Please do not reply to this email.
                </p>
            </div>
        </div>
        """

        text = f"Hi {patient_name}, your session with Dr. {counselor_name} on {appt_date} at {appt_time} starts in 10 minutes! Please open the GeneCare app or visit {meeting_link} to join."

        msg.attach(MIMEText(text, 'plain'))
        msg.attach(MIMEText(html, 'html'))

        server = smtplib.SMTP(SMTP_HOST, SMTP_PORT)
        server.starttls()
        server.login(SMTP_USERNAME, SMTP_PASSWORD)
        server.sendmail(SMTP_USERNAME, patient_email, msg.as_string())
        server.quit()

        print(f"SUCCESS: Reminder email sent to {patient_email}")
        return True

    except Exception as e:
        print(f"ERROR: {str(e)}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 7:
        print("Usage: python send_reminder_email.py <email> <patient_name> <counselor_name> <date> <time> <meeting_link>")
        sys.exit(1)

    send_email(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6])
