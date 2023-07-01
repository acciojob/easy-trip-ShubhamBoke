package com.driver.controllers;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Repository;

import java.util.*;
class Pair{
    int fId, fare;
    Pair(int fId, int fare){
        this.fId = fId;
        this.fare = fare;
    }
}
@Repository
public class AirportRepository {

    HashMap<City, Airport> airportCityHashMap = new HashMap<>();
    List<Airport> airportList = new ArrayList<>();
    HashMap<Integer, Flight> flights = new HashMap<>();
    HashMap<City, Set<Integer>> cityFlightsMap = new HashMap<>();
    HashMap<Integer, Passenger> passengers = new HashMap<>();
    HashMap<Integer, ArrayList<Integer>> flightAttendees = new HashMap<>(); //flightID vs Arraylist of passengerId
    HashMap<Integer, ArrayList<Pair>> receipt = new HashMap<>(); //PassengerId vs List if <fid, fare>
    HashMap<Integer, Integer> flightRevenue = new HashMap<>(); //flightId vs revenue


    public Airport getAirportByCity(City city) {
        return airportCityHashMap.get(city);
    }

    public void addAirport(Airport airport) {
        airportCityHashMap.put(airport.getCity(), airport);
        airportList.add(airport);
    }

    public List<Airport> getAirportList() {
        return airportList;
    }

    public Set<Integer> getFlightSet() {
        return flights.keySet();
    }
    public Set<Integer> getFlightSetTakeOff(City fromCity) {
        return cityFlightsMap.get(fromCity);
    }
    public Set<Integer> getFlightSetLanding(City toCity){
        Set<Integer> res = new HashSet<>();
        for(int key: flights.keySet()){
            Flight flight = flights.get(key);
            if(flight.getToCity() == toCity) res.add(key);
        }
        return res;
    }

    public void addFlight(Flight flight) {
        flights.put(flight.getFlightId(), flight);
        City from = flight.getFromCity();
        Set<Integer> flightSet = cityFlightsMap.getOrDefault(from, new HashSet<>());
        flightSet.add(flight.getFlightId());
        cityFlightsMap.put(from, flightSet);
    }

    public Flight getFlight(int fId) {
        return flights.get(fId);
    }

    public HashMap<Integer, Passenger> getPassengerList() {
        return passengers;
    }

    public void addPassenger(Passenger passenger) {
        this.passengers.put(passenger.getPassengerId(), passenger);
    }

    public List<Integer> getFlightAttendees(Integer flightId) {
        return flightAttendees.getOrDefault(flightId, new ArrayList<>());
    }

    public void bookTicket(Integer flightId, Integer passengerId, int fare) {
        ArrayList<Integer> pList = flightAttendees.getOrDefault(flightId, new ArrayList<>());
        pList.add(passengerId);
        flightAttendees.put(flightId, pList);
        ArrayList<Pair> faresGiven = receipt.getOrDefault(passengerId, new ArrayList<>());
        faresGiven.add(new Pair(flightId, fare));
        receipt.put(passengerId, faresGiven);
        flightRevenue.put(flightId, flightRevenue.getOrDefault(flightId, 0) + fare);
    }

    public Passenger getPassenger(Integer passengerId) {
        return passengers.get(passengerId);
    }

    public void cancelTicket(Integer flightId, Integer passengerId) {
        ArrayList<Integer> attendees = flightAttendees.get(flightId);
        attendees.remove(Integer.valueOf(passengerId));
        flightAttendees.put(flightId, attendees);
        ArrayList<Pair> faresGiven = receipt.get(passengerId);
        for(Pair p: faresGiven){
            if(p.fId == flightId){
                flightRevenue.put(flightId, flightRevenue.getOrDefault(flightId, 0) - p.fare);
                faresGiven.remove(p);
                break;
            }
        }
    }

    public int getFlightRevenue(Integer flightId) {
        return flightRevenue.get(flightId);
    }
}
