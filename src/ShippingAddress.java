public class ShippingAddress {

        private int addressID;
        private String streetNumberAndName;
        private String apartmentOrUnitNumber;
        private String city;
        private String state;
        private int zipCode;

        public int getAddressID() {
                return addressID;
        }

        public void setAddressID(int addressID) {
                this.addressID = addressID;
        }

        public String getStreetNumberAndName() {
                return streetNumberAndName;
        }

        public void setStreetNumberAndName(String streetNumberAndName) {
                this.streetNumberAndName = streetNumberAndName;
        }

        public String getApartmentOrUnitNumber() {
                return apartmentOrUnitNumber;
        }

        public void setApartmentOrUnitNumber(String apartmentOrUnitNumber) {
                this.apartmentOrUnitNumber = apartmentOrUnitNumber;
        }

        public String getCity() {
                return city;
        }

        public void setCity(String city) {
                this.city = city;
        }

        public String getState() {
                return state;
        }

        public void setState(String state) {
                this.state = state;
        }

        public int getZipCode() {
                return zipCode;
        }

        public void setZipCode(int zipCode) {
                this.zipCode = zipCode;
        }

        public String getFullAddress() {
                return streetNumberAndName + (apartmentOrUnitNumber.isEmpty() ? "" : ", " + apartmentOrUnitNumber) + ", " + city + ", " + state + " " + zipCode;
        }

        @Override
        public String toString() {
                return "Street: " + streetNumberAndName +
                        "\nApt/Unit: " + apartmentOrUnitNumber +
                        "\nCity: " + city +
                        "\nState: " + state +
                        "\nZip Code: " + zipCode;
        }

}
