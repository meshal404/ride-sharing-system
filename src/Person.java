public abstract class Person implements IPerson
{
    private final int id;
    private final String name;
    private String phoneNumber;
    private IBST<ITimeSlot, IRide> schedule;


    // Constructor
    public Person(int id, String name, String phoneNumber)
    {
        this.id = id;
        this.name = name;

        setPhoneNumber(phoneNumber); // to validate format
        this.schedule = new BST<ITimeSlot, IRide>();
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
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

    // The rides of this person, keyed by time slot
    public IBST<ITimeSlot, IRide> getSchedule()
    {
        return schedule;
    }

    public abstract String toString();

}