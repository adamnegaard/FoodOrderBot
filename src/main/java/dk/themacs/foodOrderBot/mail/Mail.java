package dk.themacs.foodOrderBot.mail;

public class Mail {
    private String mailSubject;
    private String mailFrom;
    private String mailTo;
    private String mailContent;

    public Mail(String mailSubject, String mailFrom, String mailTo, String mailContent) {
        this.mailSubject = mailSubject;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailContent = mailContent;
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
}
