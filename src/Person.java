public abstract class Person implements IPerson
{
    private final int id;
    private String name;
    private String phoneNumber;
    private LinkedList<IRide> rideHistory;

    // Constructor
    public Person(int id, String name, String phoneNumber)
    {
        this.id = id;
        this.name = name;

        setPhoneNumber(phoneNumber); // to validate format
        this.rideHistory = new LinkedList<IRide>();
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)throws IllegalArgumentException
    {
        // Must be exactly 10 number
        if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) 
        {
            throw new IllegalArgumentException("Phone number must be exactly 10 numbers.");
        }
        this.phoneNumber = phoneNumber;
    }

    public LinkedList<IRide> getRideHistory()
    {
        return rideHistory;
    }

    public abstract String toString();

}