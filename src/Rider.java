public class Rider extends Person implements IRider
{
    private final String email;
    private final String homeCity;

    public Rider(int id, String name, String email, String phone, String homeCity)
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

    public int compareTo(IRider other)
    {
        return Integer.compare(this.getId(), other.getId());
    }

    public String toString()
    {
        return "Rider [ID=" + getId() + ", Name=" + getName() + ", Phone=" + getPhoneNumber() + ", Email=" + email + ", Home City=" + homeCity + "]";
    }
}
