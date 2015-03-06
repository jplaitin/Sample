//
//  GamePickerViewController.swift
//  Ratings
//
//  Created by Jarno Petteri Laitinen on 06/03/15.
//  Copyright (c) 2015 Jarno Petteri Laitinen. All rights reserved.
//

import UIKit

protocol GamePickerViewControllerDelegate
{
    func gamePickerViewController(controller: GamePickerViewController, didSelectGame: String)
}


class GamePickerViewController: UITableViewController {
    
    var delegate: GamePickerViewControllerDelegate?
    var game: String?
    
    var _games = [String]()
    var _selectedIndex:Int?
    
    override func viewDidLoad() {
        super.viewDidLoad();
        
        _games = ["Angry Birds",
            "Chess",
            "Russian Roulette",
            "Spin the Bottle",
            "Texas Hold'em Poker",
            "Tic-Tac-Toe"]
        if(game != nil){
            _selectedIndex = find(_games, game!)
        }
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1;
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self._games.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell: UITableViewCell =  tableView.dequeueReusableCellWithIdentifier("GameCell") as UITableViewCell
        
        cell.textLabel?.text = _games[indexPath.row]
        
        if(indexPath.row == _selectedIndex){
            cell.accessoryType = UITableViewCellAccessoryType.Checkmark
        }
        else{
            cell.accessoryType = UITableViewCellAccessoryType.None
        }
        return cell;
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if(_selectedIndex != NSNotFound){
            var cell: UITableViewCell = tableView.cellForRowAtIndexPath(indexPath)!
            cell.accessoryType = UITableViewCellAccessoryType.None
        }
        _selectedIndex = indexPath.row;
        
        var cell:UITableViewCell = tableView.cellForRowAtIndexPath(indexPath)!
        cell.accessoryType = UITableViewCellAccessoryType.Checkmark
        
        var game: String = _games[indexPath.row]
        self.delegate?.gamePickerViewController(self, didSelectGame: game);
    }

}
