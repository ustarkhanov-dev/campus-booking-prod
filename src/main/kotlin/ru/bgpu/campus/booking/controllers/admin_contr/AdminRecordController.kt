package ru.bgpu.campus.booking.controllers.admin_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.bgpu.campus.booking.dto.MaintenanceRejectDto
import ru.bgpu.campus.booking.dto.RecordResponse
import ru.bgpu.campus.booking.mapper.RecordMapper
import ru.bgpu.campus.booking.services.RecordService
import java.time.LocalDate

@RestController
@RequestMapping("/admin/record") // Защищенный админский путь

@Tag(name = "Администрирование: Бронирования", description = "Управление записями системы администратором")
class AdminRecordController(private val recordService: RecordService,) {

    @GetMapping
    @Operation(
        summary = "Получение бронирований с фильтрацией и пагинацией (Для Админа)",
        description = "Позволяет смотреть все записи, либо фильтровать по конкретной дате (date), либо по диапазону (startDate + endDate)."
    )
    fun getAllRecords(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @PageableDefault(size = 20, sort = ["bookingDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<RecordResponse>> {

        val pageEntities = when {
            // 1. Если передали конкретный один день (?date=2026-10-20)
            date != null -> recordService.findAllByDate(date, pageable)

            // 2. Если передали диапазон (?startDate=...&endDate=...)
            startDate != null && endDate != null -> recordService.findByBookingDateBetween(startDate, endDate, pageable)

            // 3. Если ничего не передали — отдаем вообще всё с пагинацией
            else -> recordService.findAllWithRelations(pageable)
        }

        return ResponseEntity.ok(pageEntities.map { RecordMapper.toResponse(it) })
    }


    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Точечная отмена бронирования администратором")
    fun cancelRecordAsAdmin(@PathVariable id: Long): ResponseEntity<Void> {
        recordService.cancelRecordAsAdmin(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/maintenance-reject")
    @Operation(summary = "Массовый отказ из-за поломки/технического обслуживания оборудования")
    fun rejectForMaintenance(@Valid @RequestBody dto: MaintenanceRejectDto): ResponseEntity<Void> {
        recordService.rejectForMaintenance(dto)
        return ResponseEntity.noContent().build()
    }



}
