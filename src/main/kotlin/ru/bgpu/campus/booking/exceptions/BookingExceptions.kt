package ru.bgpu.campus.booking.exceptions


import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class BookingException(status: HttpStatus, message: String) : ResponseStatusException(status, message)

class InvalidRoomException(msg: String) : BookingException(HttpStatus.BAD_REQUEST, msg)
class PastDateException(msg: String) : BookingException(HttpStatus.BAD_REQUEST, msg)
class SlotNotAvailableException(msg: String) : BookingException(HttpStatus.CONFLICT, msg)
class BookingLimitExceededException(msg: String) : BookingException(HttpStatus.BAD_REQUEST, msg)
class AccessDeniedException(msg: String) : BookingException(HttpStatus.FORBIDDEN, msg)
