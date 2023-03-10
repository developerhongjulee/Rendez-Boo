package com.ssafy.a107.api.service;

import com.ssafy.a107.api.request.game.*;
import com.ssafy.a107.api.response.game.FastClickRes;
import com.ssafy.a107.api.response.game.BR31Res;
import com.ssafy.a107.api.response.game.GameOfDeathRes;
import com.ssafy.a107.common.exception.BadRequestException;
import com.ssafy.a107.common.exception.ConflictException;
import com.ssafy.a107.common.exception.NotFoundException;

public interface GameService {
    BR31Res createBRGameSession(BR31CreateReq br31CreateReq);

    BR31Res setBR31point(BR31Req br31Req) throws NotFoundException, ConflictException;

    GameOfDeathRes createGameOfDeathSession(GameOfDeathCreateReq gameOfDeathCreateReq);

    GameOfDeathRes runGameOfDeathSession(GameOfDeathReq gameOfDeathReq) throws NotFoundException, ConflictException;

    FastClickRes createFastClickSession(FastClickCreateReq fastClickCreateReq);

    FastClickRes runFastClickSession(FastClickReq fastClickReq) throws NotFoundException;
}
