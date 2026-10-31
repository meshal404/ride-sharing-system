public class Rider extends Person implements IRider
{
    private String email;
    private String homeCity;

    public Rider(int id, String name, String phone, String email, String homeCity)
    {
        super(id, name, phone);
        this.email = email;
        this.homeCity = homeCity;
    }

    public String getEmail()
    {
        return email;
    }

    public String getHomeCity()
    {
        return homeCity;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setHomeCity(String homeCity)
    {
        this.homeCity = homeCity;
    }

    public int compareTo(IRider other)
    {
        return Integer.compare(this.getId(), other.getId());
    }

    public String toString()
    {
        return "Rider [ID=" + getId() + ", Name=" + getName() + ", Phone=" + getPhoneNumber() + ", Email=" + email + ", Home City=" + homeCity + "]";
    }
}
