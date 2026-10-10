from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class MedicineRecord:
    name: str
    form: str
    price: float
    days_left: int
    prescription: bool
