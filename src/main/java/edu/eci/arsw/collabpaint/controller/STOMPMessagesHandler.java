package edu.eci.arsw.collabpaint.controller;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Controller
public class STOMPMessagesHandler {

    @Autowired
    SimpMessagingTemplate msgt;

    private final ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Point>>  polygons = new ConcurrentHashMap<>();

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {
        System.out.println("Nuevo punto recibido en el servidor!:"+pt);
        Integer num = Integer.parseInt(numdibujo);
        if (!polygons.containsKey(num)) {
            ConcurrentLinkedDeque<Point> points = new ConcurrentLinkedDeque<>(Collections.singletonList(pt));
            polygons.put(num, points);
        } else {
            polygons.get(num).add(pt);
        }
        if (polygons.get(num).size() == 4) {
            msgt.convertAndSend("/topic/newpolygon." + numdibujo, polygons.get(num));
            polygons.remove(num);
        }
        msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
    }
}
