package dk.themacs.foodOrderBot.mail;

public class Mail {
    private String mailSubject;
    private String mailFrom;
    private String mailFromName;
    private String mailTo;
    private String mailContent;
    private String mailCc;

    public Mail(String mailSubject, String mailFrom, String mailFromName, String mailTo, String mailContent, String mailCc) {
        this.mailSubject = mailSubject;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.mailTo = mailTo;
        this.mailContent = mailContent;
        this.mailCc = mailCc;
    }


    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailFromName() {
        return mailFromName;
    }

    public void setMailFromName(String mailFromName) {
        this.mailFromName = mailFromName;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }

    public String getMailCc() {
        return mailCc;
    }

    public void setMailCc(String mailCc) {
        this.mailCc = mailCc;
    }
}
