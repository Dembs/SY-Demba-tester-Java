package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        //Récupération du ticket dans la base de données
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        //Vérification que le numéro d'immatriculation est le bon
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());

        //Vérification que la place de parking n'est plus valable
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit() throws Exception {
        //Simuler un véhicule garé
        testParkingACar();

        // Simuler une durée d'une heure sur le ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticket.setInTime(inTime);

        ticketDAO.updateInTime(ticket);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // Récupérer le ticket mis à jour depuis la base de données
        ticket = ticketDAO.getTicket("ABCDEF");
        //Vérifier que le temps de sortie est bien présent dans la base de données
        assertNotNull(ticket.getOutTime());

        // Vérifier que le prix calculé correspond au prix  d'une heure de stationnement
        assertEquals(ticket.getPrice(),Fare.CAR_RATE_PER_HOUR*1,0.01);
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        // Simuler un véhicule déjà venu
        testParkingLotExit();
        //Simuler un véhicule garé
        testParkingACar();

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Simuler une durée d'une heure sur le ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticket.setInTime(inTime);
        ticketDAO.updateInTime(ticket);

        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");

        // Vérifier que le prix calculé correspond au prix avec réduction d'une heure de stationnement
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR *1*0.95,0.01);
    }
}
