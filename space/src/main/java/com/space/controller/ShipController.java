package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {

    private ShipService shipService;

    public ShipController() {

    }

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @RequestMapping(value = "/ships", method = RequestMethod.GET)
    public List<Ship> getAllShips(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        List<Ship> shipList = shipService.getShips(name, planet, shipType, after, before, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        List<Ship> sortedShipList = shipService.sortShips(shipList, order);
        return shipService.getPage(sortedShipList, pageNumber, pageSize);
    }

    @RequestMapping(value = "/ships/count", method = RequestMethod.GET)
    public Integer shipsCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ) {
        return shipService.getShips(name, planet, shipType, after, before, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();
    }

    @RequestMapping(path = "/ships/{id}", method = RequestMethod.GET)
    public ResponseEntity<Ship> getShip(@PathVariable(value = "id") String pathId) {
        Long id = convertIdToLong(pathId);
        if (id == null || id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getShip(id);
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @RequestMapping(path = "/ships", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if (!shipService.isShipValid(ship)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        ship.setSpeed(ship.getSpeed());
        ship.setRating(shipService.computeRating(ship.getSpeed(), ship.getUsed(), ship.getProdDate()));
        Ship saveShip = shipService.saveShip(ship);
        return new ResponseEntity<>(saveShip, HttpStatus.OK);
    }

    @RequestMapping(path = "/ships/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Ship> updateShip(@PathVariable(value = "id") String pathId,
            @RequestBody Ship ship) {
        ResponseEntity<Ship> entity = getShip(pathId);
        Ship savedShip = entity.getBody();
        if (savedShip == null) {
            return entity;
        }

        Ship result;
        try {
            result = shipService.updateShip(savedShip, ship);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/ships/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Ship> deleteShip(@PathVariable(value = "id") String pathId) {
        ResponseEntity<Ship> entity = getShip(pathId);
        Ship ship = entity.getBody();
        if (ship == null) {
            return entity;
        }
        shipService.deleteShip(ship);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Long convertIdToLong(String pathId) {
        if (pathId == null) {
            return null;
        } else try {
            return Long.parseLong(pathId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
