package com.shop.cli;

import com.shop.model.Good;
import com.shop.rest.RestService;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleInterface implements Runnable{

    public static final int THE_ANSWER_TO_THE_MAIN_QUESTION_OF_THE_UNIVERSE = 42;

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    private RestService restService;

    private List<Good> goods;

    @SneakyThrows
    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter username");
        String username = in.nextLine();
        System.out.println("Enter password");
        String password = in.nextLine();
        restService = new RestService(username, password);
        if(!checkConnection()) {
            System.exit(THE_ANSWER_TO_THE_MAIN_QUESTION_OF_THE_UNIVERSE);
        }
        System.out.println("Connection was established\nType: \"help\" for information about all commands");
        String command;
        while (true) {
            if(in.hasNext()) {
                command = in.nextLine();
                if(command.contains("get")) {
                    goods = restService.getAllGoods();
                    for (Good good : goods) {
                        System.out.println(good.toString());
                    }
                }
                if(command.contains("help")) {
                    System.out.println("Get goods list usage: get");
                    System.out.println("Buy goods usage: buy <id> <count>");
                    System.out.println("(Admin option) Add goods usage: add <id> <count>");
                }
                if(command.contains("buy")) {
                    String[] commandParts = command.split(" ");
                    Integer id = Integer.parseInt(commandParts[1]);
                    Integer count = Integer.parseInt(commandParts[2]);
                    if(restService.buyGoods(id, count).contains("OK")) {
                        System.out.println("You have bought " + count + " items with id: " + id);
                    } else {
                        System.out.println(ANSI_RED + "Sorry but warehouse cant offer you " + count + " items with id: " + id + ANSI_RESET);
                    }
                }
                if(command.contains("add")) {
                    String[] commandParts = command.split(" ");
                    int id = Integer.parseInt(commandParts[1]);
                    int count = Integer.parseInt(commandParts[2]);
                    try {
                        if(restService.addGood(Good.builder()
                                .id(id)
                                .name(findById(id).getName())
                                .count(count)
                                .build()).contains("OK")) {
                            System.out.println("You have add item with id: " + id + " count: " + count);
                        } else {
                            System.out.println(ANSI_RED + "Something went wrong" + ANSI_RESET);
                        }
                    } catch (IllegalArgumentException ex) {
                        System.out.println(ANSI_RED + "Wrong id" + ANSI_RESET);
                    }
                }
            }
        }
    }

    private boolean checkConnection() {
        try {
            goods = restService.getAllGoods();
            return true;
        } catch (IOException e) {
            System.out.println(ANSI_RED + "Connection is not established\nMaybe wrong credentials???" + ANSI_RESET);
        }
        return false;
    }

    private Good findById(Integer id) {
        List<Good> filtered = goods
                .stream()
                .filter(x -> x.getId() == id)
                .collect(Collectors.toList());
        if(filtered.size() == 0) {
            throw new IllegalArgumentException();
        }
        return filtered.get(0);
    }
}
