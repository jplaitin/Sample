//
//  PlayersViewController.swift
//  Ratings
//
//  Created by Jarno Petteri Laitinen on 04/03/15.
//  Copyright (c) 2015 Jarno Petteri Laitinen. All rights reserved.
//
import UIKit

class PlayersViewController: UITableViewController, PlayerDetailsViewControllerDelegate {
    
    var players = [Player]();
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.players.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> PlayerCell {
        var cell = tableView.dequeueReusableCellWithIdentifier("PlayerCell") as PlayerCell
        
        var player: Player = self.players[indexPath.row]
        
        cell.nameLabel.text = player.name;
        cell.gameLabel.text = player.game;
        cell.ratingImageView.image = imageForRating(player.rating)
        return cell
    }
    
    func imageForRating(rating: Int) -> UIImage? {
        switch (rating){
            case 1: println("-->1StarSmall")
                return UIImage(named: "1StarSmall")
            case 2: println("-->2StarsSmall")
                return UIImage(named: "2StarsSmall")
            case 3: println("-->3StarsSmall")
                return UIImage(named: "3StarsSmall")
            case 4: println("-->4StarsSmall")
                return UIImage(named: "4StarsSmall")
            case 5: println("-->5StarsSmall")
                return UIImage(named: "5StarsSmall")
            default: return nil;
        }
    }
    
    func playerDetailsViewControllerDidCancel(controller: PlayerDetailsViewController) {
        self.dismissViewControllerAnimated(true, completion: nil);
        println("playerDetailsViewControllerDidCancel");
    }
    
    func playerDetailsViewControllerDidSave(controller: PlayerDetailsViewController, didAdd: Player) {
        players.append(didAdd);
        
        var indexPath: NSIndexPath = NSIndexPath(forRow: players.count-1, inSection: 0);
        self.tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Automatic)
        
        self.dismissViewControllerAnimated(true, completion: nil);
        println("playerDetailsViewControllerDidSave");
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue?, sender: AnyObject?) {
        if (segue!.identifier == "AddPlayer") {
            var navigationController: UINavigationController = segue?.destinationViewController as UINavigationController
            var playerDetailsViewController: PlayerDetailsViewController = navigationController.viewControllers[0] as PlayerDetailsViewController
            playerDetailsViewController.delegate = self;
        }
    }
}
