public class Driver extends Person implements IDriver
{
    private final String vehiclePlate;
    private final VehicleType vehicleType;

    public Driver(int id, String name, String phone, String plate, VehicleType type)
    {
        super(id, name, phone);
        validatePlate(plate); //for validateing format
        this.vehiclePlate = plate;
        vehicleType = type;
    }

    public String getVehiclePlate()
    {
        return vehiclePlate;
    }

    public VehicleType getVehicleType()
    {
        return vehicleType;
    }

    public void validatePlate(String plate) throws IllegalArgumentException
    {
        //checking the format: 3 uppercase letters then 4 numbers
        if (plate == null || !plate.matches("^[A-Z]{3}\\d{4}$")) 
        {
            throw new IllegalArgumentException("Vehicle plate format must be exactly 3 uppercase letters followed by 4 numbers");
        }
    }

    public int compareTo(IDriver other)
    {
        return Integer.compare(this.getId(), other.getId());
    }

    public String toString() {
        return "Driver [ID=" + getId() + ", Name=" + getName() + ", Phone=" + getPhoneNumber() + ", Vehicle Plate=" + vehiclePlate + ", Vehicle Type=" + vehicleType + "]";
    }
}
