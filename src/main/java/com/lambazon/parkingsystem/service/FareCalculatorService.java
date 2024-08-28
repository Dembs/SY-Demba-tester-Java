package com.lambazon.parkingsystem.service;

import com.lambazon.parkingsystem.constants.Fare;
import com.lambazon.parkingsystem.model.Ticket;

public class FareCalculatorService {

    // Default : no discount
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket,boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //Conversion en millisecondes
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        //Conversion en heure et utilisation des double
        double duration = (double) (outHour - inHour) /(60000*60);

        //US1 : Free parking for 30min
        if (duration <= 0.5){
            duration = 0;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        // US 2 : Apply discount if the discount flag is true
        if (discount) {
            ticket.setPrice(ticket.getPrice() * 0.95);
        }
    }


}