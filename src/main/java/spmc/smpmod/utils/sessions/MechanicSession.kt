package spmc.smpmod.utils.sessions

abstract class MechanicSession {

	abstract fun tick(): Boolean
}