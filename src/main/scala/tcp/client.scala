package tcp

import java.net.{InetAddress, Socket }
import java.io._

import scala.io._
import collection.parallel._

import runtime._
import game_mechanics._
import game_mechanics.bunny._
import game_mechanics.tower._
import game_mechanics.path._
import gui._
import gui.animations._
import tcp.packets._

import scala.collection.mutable.{ListBuffer,Queue}

class ClientThread(domain : String, name: String)
extends Thread("ClientThread")
{
    val socket = new Socket(InetAddress.getByName(domain),Server.default_port)
    val in = new ObjectInputStream(
        new DataInputStream(socket.getInputStream()))
    val out = new ObjectOutputStream(
        new DataOutputStream(socket.getOutputStream()))
    val queue  = new Queue[Any]()
    val player = new Player(name)
    var running = true

    def add(arg : Any): Unit = {
        queue.enqueue(arg)
    }

    def send(arg : Any) : Unit = {
        this.synchronized {
            println("Sending " + arg.toString)
            out.writeObject(arg)
            out.flush()
        }
    }

    var handle : Any => Unit = { packet =>
        println("Lobby received " + packet.toString)
        packet match {
            case PlayerIdPacket(id) => {
                player.id = id
            }
            case GameStartPacket(map,serplayers) => {
                val players = new ListBuffer[Player]
                for( (id,name,base) <- serplayers )
                {
                    val ply = new Player(name)
                    ply.id = id
                    ply.base = base
                    players += ply
                }
                StateManager.set_state(
                    new ClientGameState(players(player.id),
                        players, map, this) )
            }
        }
    }

    def receive() : Unit = {
        val packet = in.readObject()
        handle(packet)
    }

    class Receiver extends Thread("ServerReceiver")
    {
        override def run() : Unit = {
            try {
                while(true)
                    receive()
            }
            catch
            {
                case e : IOException =>
                    StateManager.set_state( new ErrorMenuState(
                        e.toString, MultiplayerMenuState ) )
            }
        }
    }

    def close() = {
        out.close()
        in.close()
        socket.close()
    }

    override def run(): Unit = {
        send(PlayerInfoPacket(name))
            while(running) {
                if (!queue.isEmpty) {
                    send(queue.dequeue())
                }
            }
    }

    new Receiver().start()
        /*
    }
    catch {
        case e: IOException =>
            e.printStackTrace()
    }
    */
}

// vim: set ts=4 sw=4 et:
