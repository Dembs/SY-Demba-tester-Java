package com.lambazon.parkingsystem;

import com.lambazon.parkingsystem.constants.ParkingType;
import com.lambazon.parkingsystem.dao.ParkingSpotDAO;
import com.lambazon.parkingsystem.dao.TicketDAO;
import com.lambazon.parkingsystem.model.ParkingSpot;
import com.lambazon.parkingsystem.model.Ticket;
import com.lambazon.parkingsystem.service.ParkingService;
import com.lambazon.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private  ParkingService parkingService;

    @Mock
    private  InputReaderUtil inputReaderUtil;
    @Mock
    private  ParkingSpotDAO parkingSpotDAO;
    @Mock
    private  TicketDAO ticketDAO;

    @BeforeEach
    void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO,Mockito.times(1)).getNbTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

    }

    @Test
    public void processExitingVehicleTestUnableUpdate()throws Exception {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
            when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

            parkingService.processExitingVehicle();

            // Vérifications
            verify(ticketDAO, times(1)).getTicket(anyString());
            verify(ticketDAO, times(1)).getNbTicket(anyString());
            verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
            verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    }
    @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO,Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
        verify(parkingSpotDAO,Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO,Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO,Mockito.times(1)).getNbTicket(anyString());
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
            when(inputReaderUtil.readSelection()).thenReturn(1); // 1 pour CAR
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertNotNull(parkingSpot);
            assertEquals(1, parkingSpot.getId());
            assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
    }


    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
            when(inputReaderUtil.readSelection()).thenReturn(1); // 1 pour CAR
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertNull(parkingSpot);

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
            when(inputReaderUtil.readSelection()).thenReturn(3);

            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertNull(parkingSpot);
    }
}
