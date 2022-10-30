package com.supercom.puretrack.data.cycle.resultObjects.victim

import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult

class GetVictimConfigurationResult: BaseTaskResult() {
   lateinit var data: List<VictimData>
}