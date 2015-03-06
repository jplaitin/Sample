//
//  AppDelegate.swift
//  Ratings
//
//  Created by Jarno Petteri Laitinen on 04/03/15.
//  Copyright (c) 2015 Jarno Petteri Laitinen. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    lazy var _players = [Player]();

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
        // Override point for customization after application launch.
        
        var player = Player();
        player.name = "Bill Evans";
        player.game = "Tic-Tac-Toe";
        player.rating = 4;
        _players.append(player);
        
        player = Player();
        player.name = "Oscar Peterson";
        player.game = "Spin the Bottle";
        player.rating = 5;
        _players.append(player);


        player = Player();
        player.name = "Dave Brubeck";
        player.game = "Texas Hold’em Poker";
        player.rating = 2;
        _players.append(player);
        
        var tabBarController: UITabBarController = window?.rootViewController as UITabBarController;
        var navigationContorller: UINavigationController = tabBarController.viewControllers![0] as UINavigationController;
        var playersViewController: PlayersViewController = navigationContorller.viewControllers![0] as PlayersViewController;
        
        playersViewController.players = _players;
        return true
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

