package com.driver.controllers;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AirportService {
    AirportRepository airportRepository = new AirportRepository();

    public boolean addAirport(Airport airport) {
        List<Airport> apList = airportRepository.getAirportList();
        for(Airport ap: apList){
            if(ap.getAirportName().equals(airport.getAirportName()) || ap.getCity() == airport.getCity())
                return false;
        }
        airportRepository.addAirport(airport);
        return true;
    }

    public String getLargestAirport() {
        List<Airport> apList = airportRepository.getAirportList();
        Airport res = apList.get(0);
        for(Airport airport: apList){
            if(airport.getNoOfTerminals() > res.getNoOfTerminals())
                res = airport;
            else if(airport.getNoOfTerminals() == res.getNoOfTerminals() && airport.getAirportName().compareTo(res.getAirportName()) < 0)
                res = airport;
        }
        return res.getAirportName();
    }

    public boolean addFlight(Flight flight) {
        Set<Integer> fList = airportRepository.getFlightSet();
        if(fList.contains(flight.getFlightId()))
            return false;

        List<Airport> apList = airportRepository.getAirportList();
        Set<City> cities = new HashSet<>();
        for(Airport ap: apList){
            cities.add(ap.getCity());
        }
        if(!cities.contains(flight.getFromCity()) || !cities.contains(flight.getToCity())) return false;


        airportRepository.addFlight(flight);
        return true;
    }

    public double getShortestTime(City fromCity, City toCity) {
        Set<Integer> flights = airportRepository.getFlightSetTakeOff(fromCity);
        if(flights == null)
            return -1;
        double res = Double.MAX_VALUE;
        for(int fId: flights){
            Flight flight = airportRepository.getFlight(fId);
            if(flight.getToCity() == toCity && flight.getDuration() < res)
                res = flight.getDuration();
        }
        return res;
    }

    public int getTotalPeopleOnAirport(Date date, String airportName) {
        List<Airport> apList = airportRepository.getAirportList();
        Airport airport = null;
        for(Airport ap: apList){
            if(ap.getAirportName().equals(airportName)){
                airport = ap;
                break;
            }
        }
        if(airport == null) return -1;

        City city = airport.getCity();
        Set<Integer> flightListOutgoing = airportRepository.getFlightSetTakeOff(city);
        Set<Integer> flightListIncoming = airportRepository.getFlightSetLanding(city);
        if(flightListIncoming == null || flightListOutgoing == null) return 0;

        int count = 0;
        for(int fId: flightListIncoming){
            Date fdate = airportRepository.getFlight(fId).getFlightDate();
            if(fdate.getMonth() == date.getMonth() && fdate.getDate() == date.getDate() && fdate.getYear() == date.getYear()){
                count += airportRepository.getFlightAttendees(fId).size();
            }

        }
        for(int fId: flightListOutgoing){
            Date fdate = airportRepository.getFlight(fId).getFlightDate();
            if(fdate.getMonth() == date.getMonth() && fdate.getDate() == date.getDate() && fdate.getYear() == date.getYear()){
                count += airportRepository.getFlightAttendees(fId).size();
            }

        }
        return count;
    }

    public boolean addPassenger(Passenger passenger) {
        HashMap<Integer, Passenger> passList = airportRepository.getPassengerList();
        if(!passList.keySet().contains(passenger.getPassengerId())){
            airportRepository.addPassenger(passenger);
            return true;
        }
        return false;
    }

    public boolean bookTicket(Integer flightId, Integer passengerId) {
        List<Integer> attendees = airportRepository.getFlightAttendees(flightId);
        Flight flight = airportRepository.getFlight(flightId);
        Passenger p = airportRepository.getPassengerList().get(passengerId);
        if(p == null || flight == null) return false;
        if(attendees == null || attendees.contains(passengerId) || attendees.size() >= flight.getMaxCapacity()) return false;

        airportRepository.bookTicket(flightId, passengerId, calculateFare(flightId));
        return true;
    }

    public int calculateFare(Integer flightId) {
        int attendeesCount = airportRepository.getFlightAttendees(flightId).size();
        return 3000 + attendeesCount*50;
    }

    public boolean cancelTicket(Integer flightId, Integer passengerId) {
        Flight flight = airportRepository.getFlight(flightId);
        Passenger passenger = airportRepository.getPassenger(passengerId);
        if(flight == null || passenger == null) return false;

        List<Integer> attendees = airportRepository.getFlightAttendees(flightId);
        if(!attendees.contains(passengerId)) return false;

        airportRepository.cancelTicket(flightId, passengerId);
        return true;
    }

    public int getTicketCountByPassenger(Integer passengerId) {
        Set<Integer> flights = airportRepository.getFlightSet();
        int count = 0;
        for(int fId: flights){
            List<Integer> attendees = airportRepository.getFlightAttendees(fId);
            if(attendees.contains(passengerId)) count++;
        }
        return count;
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        Flight flight = airportRepository.getFlight(flightId);
        if(flight == null) return null;
        City city = flight.getFromCity();
        Airport airport = airportRepository.getAirportByCity(city);
        return airport.getAirportName();
    }

    public int calculateRevenue(Integer flightId) {
        return airportRepository.getFlightRevenue(flightId);
    }
}
