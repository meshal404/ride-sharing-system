public class RiderList implements IRiderList
{
    private Node<IRider> head;
    private int count;

    public RiderList()
    {
        head = null;
        count = 0;
    }

    public boolean add(IRider rider)
    {
        if (rider == null)
            return false;

        Node<IRider> tmp = new Node<IRider>(rider);

        // If list is empty or new rider ID is smaller than head ID
        if(head == null || head.data.getId() > rider.getId())
        {
            tmp.next = (head);
            head = tmp;
            count++;
            return true;
        }

        Node<IRider> current = head;
        while (current.next != null && current.next.data.getId() < rider.getId()) 
        {
            current = current.next;
        }

        // Check if a rider with the same ID already exists 
        if (current.data.getId() == rider.getId() || (current.next != null && current.next.data.getId() == rider.getId())) 
        {
            return false;
        }

        tmp.next = current.next;
        current.next = tmp;
        count++;
        return true;
    }

    public IRider findById(int riderId)
    {
        Node<IRider> current = head;
        // moving through the list to find the rider
        while(current != null)
        {
            if(current.data.getId() == riderId)
                return current.data;

            current = current.next;
        }

        return null;
    }

    public LinkedList<IRider> findByName(String fullName)
    {
        LinkedList<IRider> result = new LinkedList<IRider>();
        Node<IRider> current = head;

        // moving through the list and adding each match to result
        while (current != null)
        {
            if(current.data.getName().equals(fullName))
                result.insert(current.data);

            current = current.next;
        }

        return result;
    }

    public IRider findByEmail(String email)
    {
        Node<IRider> current = head;

        //moving through the list to find match
        while (current != null) 
        {
            if (current.data.getEmail().equals(email)) 
                return current.data;

            current = current.next;
        }

        return null;
    }

    public LinkedList<IRider> findByHomeCity(String homeCity) 
    {
        LinkedList<IRider> result = new LinkedList<IRider>();
        Node<IRider> current = head;

        // moving through the list and adding each match to result
        while (current != null) 
        {
            if (current.data.getHomeCity().equals(homeCity))
                result.insert(current.data);
            
            current = current.next;
        }

        return result;
    }

    //Creating a new list because the interface requires it
    public LinkedList<IRider> getAll() 
    {
        LinkedList<IRider> result = new LinkedList<IRider>();
        Node<IRider> current = head;

        while (current != null) 
        {
            result.insert(current.data);
            current = current.next;
        }

        return result;
    }

    public boolean removeById(int riderId)
    {
        // Case 1: the head is the match
        if (head != null && head.data.getId() == riderId)
        {
            head = head.next;
            count--;
            return true;
        }

        // Case 2: look one node ahead
        Node<IRider> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getId() == riderId)
            {
                prev.next = prev.next.next;   
                count--;
                return true;                   
            }
            prev = prev.next;
        }
        return false;
    }

    public boolean removeByEmail(String email) 
    {
        // Case 1: the head is the match
        if (head != null && head.data.getEmail().equals(email))
        {
            head = head.next;
            count--;
            return true;
        }

        // Case 2: look one node ahead
        Node<IRider> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getEmail().equals(email))
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
        Node<IRider> prev = head;
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

    public int removeByHomeCity(String homeCity)
    {

        int removed = 0;

        // Remove matches at the front of the list
        while (head != null && head.data.getHomeCity().equals(homeCity))
        {
            head = head.next;
            removed++;
        }

        // Remove matches in the rest of the list
        Node<IRider> prev = head;
        while (prev != null && prev.next != null)
        {
            if (prev.next.data.getHomeCity().equals(homeCity))
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
