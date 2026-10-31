public class DriverList implements IDriverList
{
    private Node<IDriver> head;
    private int count;

    public DriverList()
    {
        head = null;
        count = 0;
    }

    public boolean add(IDriver driver)
    {
        if (driver == null)
            return false;

        if (findByVehiclePlate(driver.getVehiclePlate()) != null) 
            return false;

        Node<IDriver> tmp = new Node<IDriver>(driver);

        // If list is empty or new driver ID is smaller than head ID
        if(head == null || head.data.getId() > driver.getId())
        {
            tmp.next = (head);
            head = tmp;
            count++;
            return true;
        }

        Node<IDriver> current = head;
        while (current.next != null && current.next.data.getId() < driver.getId()) 
        {
            current = current.next;
        }

        // Check if a driver with the same ID already exists 
        if (current.data.getId() == driver.getId() || (current.next != null && current.next.data.getId() == driver.getId())) 
        {
            return false;
        }

        tmp.next = current.next;
        current.next = tmp;
        count++;
        return true;
    }

    public IDriver findById(int driverId)
    {
        Node<IDriver> current = head;
        // moving through the list to find the driver
        while(current != null)
        {
            if(current.data.getId() == driverId)
                return current.data;

            current = current.next;
        }

        return null;
    }

    public LinkedList<IDriver> findByName(String fullName)
    {
        LinkedList<IDriver> result = new LinkedList<IDriver>();
        Node<IDriver> current = head;

        // moving through the list and adding each match to result
        while (current != null)
        {
            if(current.data.getName().equals(fullName))
                result.insert(current.data);

            current = current.next;
        }

        return result;
    }

    public IDriver findByVehiclePlate(String plate)
    {
        Node<IDriver> current = head;

        //moving through the list to find match
        while (current != null) 
        {
            if (current.data.getVehiclePlate().equals(plate)) 
                return current.data;

            current = current.next;
        }

        return null;
    }

    public LinkedList<IDriver> findByVehicleType(VehicleType type) 
    {
        LinkedList<IDriver> result = new LinkedList<IDriver>();
        Node<IDriver> current = head;

        // moving through the list and adding each match to result
        while (current != null) 
        {
            if (current.data.getVehicleType().equals(type))
                result.insert(current.data);
            
            current = current.next;
        }

        return result;
    }

    //Creating a new list because the interface requires it
    public LinkedList<IDriver> getAll() 
    {
        LinkedList<IDriver> result = new LinkedList<IDriver>();
        Node<IDriver> current = head;

        while (current != null) 
        {
            result.insert(current.data);
            current = current.next;
        }

        return result;
    }

    public boolean removeById(int driverId)
    {
        // Case 1: the head is the match
        if (head != null && head.data.getId() == driverId)
        {
            head = head.next;
            count--;
            return true;
        }

        // Case 2: look one node ahead
        Node<IDriver> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getId() == driverId)
            {
                prev.next = prev.next.next;   
                count--;
                return true;                   
            }
            prev = prev.next;
        }
        return false;
    }

    public boolean removeByVehiclePlate(String plate) 
    {
        // Case 1: the head is the match
        if (head != null && head.data.getVehiclePlate().equals(plate))
        {
            head = head.next;
            count--;
            return true;
        }

        // Case 2: look one node ahead
        Node<IDriver> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getVehiclePlate().equals(plate))
            {
                prev.next = prev.next.next;   
                count--;
                return true;                   
            }
            prev = prev.next;
        }
        return false;
    }

    public int removeByName(String fullName)
    {
        int removed = 0;

        // Remove matches at the front of the list
        while (head != null && head.data.getName().equals(fullName))
        {
            head = head.next;
            removed++;
        }

        // Remove matches in the rest of the list
        Node<IDriver> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getName().equals(fullName))
            {
                prev.next = prev.next.next;   
                removed++;
            }
            else
            {
                prev = prev.next;             
            }
        }

        count -= removed;
        return removed;
    }

    public int removeByVehicleType(VehicleType type)
    {

        int removed = 0;

        // Remove matches at the front of the list
        while (head != null && head.data.getVehicleType().equals(type))
        {
            head = head.next;
            removed++;
        }

        // Remove matches in the rest of the list
        Node<IDriver> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getVehicleType().equals(type))
            {
                prev.next = prev.next.next;   
                removed++;
            }
            else
            {
                prev = prev.next;             
            }
        }

        count -= removed;
        return removed;
    }

    public int size()
    {
        return count;
    }
}
