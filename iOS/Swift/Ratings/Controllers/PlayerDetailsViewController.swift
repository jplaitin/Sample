//
//  PlayerDetailsViewController.swift
//  Ratings
//
//  Created by Jarno Petteri Laitinen on 05/03/15.
//  Copyright (c) 2015 Jarno Petteri Laitinen. All rights reserved.
//
import UIKit

protocol PlayerDetailsViewControllerDelegate
{
    func playerDetailsViewControllerDidCancel(controller: PlayerDetailsViewController)
    func playerDetailsViewControllerDidSave(controller: PlayerDetailsViewController, didAdd:Player)
}

class PlayerDetailsViewController: UITableViewController, GamePickerViewControllerDelegate {
    
    var delegate: PlayerDetailsViewControllerDelegate?
    @IBOutlet weak var detailLabel: UILabel!
    @IBOutlet weak var nameTextField: UITextField!
    
    var _game:String?
    
    
    @IBAction func cancel(sender: AnyObject) {
        delegate?.playerDetailsViewControllerDidCancel(self)
    }

    @IBAction func done(sender: AnyObject) {
        var player: Player = Player();
        player.name = self.nameTextField.text;
        player.game = _game!
        player.rating = 1;
        
        delegate?.playerDetailsViewControllerDidSave(self, didAdd:player)
        
    
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.detailLabel.text = _game
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if (indexPath.section == 0) {
            self.nameTextField.becomeFirstResponder()
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == "PickGame") {
            var gamePickerViewController: GamePickerViewController = segue.destinationViewController as GamePickerViewController
            gamePickerViewController.delegate = self
            gamePickerViewController.game = _game;
        }
    }

    func gamePickerViewController(controller: GamePickerViewController, didSelectGame: String) {
        _game = didSelectGame
        self.detailLabel.text = _game
        self.navigationController?.popViewControllerAnimated(true)
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        _game = "Chess"
        NSLog("init PlayerDetailsViewController");
    }
    
    deinit {
        //A deinitializer is called immediately before a class instance is deallocated. 
        NSLog("dealloc PlayerDetailsViewController");
    }
}